
package com.synchrony.networking;

import com.synchrony.config.Config;
import com.synchrony.config.Config.Watcher;
import com.synchrony.core.DirHasher;
import com.synchrony.core.FSFolder;
import com.synchrony.ui.SynchronyUIManager;
import com.synchrony.ui.notification.NotificationService;
import com.synchrony.util.IOUtils;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Starts the client.
 * @author blip
 * @author mbien
 */
public class Synchrony {

    private static final Logger LOG = Logger.getLogger(Synchrony.class.getName());

    public static void main(String[] args) {
        
        Config config = readConfig();
        
        final SynchronyUIManager ui = new SynchronyUIManager(config);
        ui.init();
        
        DirHasher observer = null;
        //TODO we use currently only one folder (the first in the config)
        List<Config.Watcher> watchers = config.watchers;
        if(!watchers.isEmpty()) {
            try {
                observer = new DirHasher(Paths.get(watchers.get(0).path));
                observer.init();
            } catch (IOException ex) {
                LOG.log(Level.SEVERE, "can not start dir hasher.", ex);
            }
        }
        
        final DirHasher _observer = observer;
        
        NodeListener nodeListener = new NodeListener() {

            @Override
            public void nodeDiscovered(Node node, List<Node> all) {
                System.out.println("new node "+node);
                System.out.println("all nodes: "+all);
                try {
                    node.initialSync(_observer.getSnapshot());
                } catch (IOException ex) {
                    LOG.log(Level.SEVERE, null, ex);
                }
                
                ui.getNotificationService().showNotification("synchrony", "initial sync with "+node+" finished");
            }

            @Override
            public void nodeLost(Node node, List<Node> all) {
                System.out.println("node died "+node);
                System.out.println("all nodes: "+all);
                
                ui.getNotificationService().showNotification("synchrony", node+" disconnected");
            }
        };
        
        MessageListener msgListener = new MessageListener() {
            @Override
            public void onMessage(MsgHeader message, Socket connection) {
                
                if(message.equals(MsgHeader.STATUS_REQUEST)) {
                    try (ObjectOutputStream oos = new ObjectOutputStream(connection.getOutputStream())) {
                        FSFolder snapshot = _observer.getSnapshot();
                        oos.writeObject(snapshot);
                    } catch (IOException ex) {
                        LOG.log(Level.SEVERE, null, ex);
                    }
                } else if(message.equals(MsgHeader.SYNC_UPLOAD)) {
                    try (InputStream is = connection.getInputStream();
                         ObjectInputStream ois = new ObjectInputStream(is)) {
                        
                        ByteBuffer buffer = IOUtils.newDirectByteBuffer(1024*200);
                        
                        String relPath = (String)ois.readObject();
                        Path destPath = _observer.getRootDir().resolve(relPath);
                        System.out.println("dest path: "+destPath);
                        
                        Files.createDirectories(destPath.getParent());
                        destPath.createFile();
                        
                        try (ReadableByteChannel inputChannel = Channels.newChannel(is);
                             WritableByteChannel outputChannel = destPath.newByteChannel(StandardOpenOption.WRITE)) {
                            
                            IOUtils.transfer(inputChannel, outputChannel, buffer);
                        }
                        
                    } catch (final IOException | ClassNotFoundException ex) {
                        LOG.log(Level.SEVERE, null, ex);
                    }
                }
                
            }
        };

        // networking
        SynchronyHost host = new SynchronyHost(config, nodeListener, msgListener);
        host.startHost();
        
        // start listening on system events
        try {
            observer.processEvents();
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, "can not start dir watcher.", ex);
        }
    }

    private static Config readConfig() {
        
        String configBase = System.getProperty("synchrony.config", System.getProperty("user.home"));
        Path configFolder = Paths.get(configBase).resolve(".synchrony/");
        Path configPath = configFolder.resolve("config.xml");
        
        Config config = null;
        if(!configFolder.exists() || !configPath.exists()) {
            config = createDefaultConfig(configFolder, config, configPath);
        }else{
            try {
                LOG.info("reading configuration ["+configFolder+"]");
                config = Config.read(configPath.toUri());
            } catch (IOException ex) {
                // TODO maybe a little bit to radical
                LOG.log(Level.WARNING, "can not read configuration, overwriting with default", ex);
                config = createDefaultConfig(configFolder, config, configPath);
            }
        }
        return config;
    }
        
    private static Config createDefaultConfig(Path configFolder, Config config, Path configPath) {
        try {
            LOG.info("no configuration found, creating default.");
            if (!configFolder.exists()) {
                configFolder.createDirectory();
            }
            config = Config.getDefault();
            
            //TODO remove as soon UI is working
            Watcher watcher = new Config.Watcher();
            watcher.path = System.getProperty("user.home")+"/test";
            watcher.enabled = true;
            watcher.name = "test watcher";
            config.watchers.add(watcher);
            
            config.save(configPath.toUri());
        } catch (IOException ex) {
            throw new RuntimeException("can not create default configuration", ex);
        }
        return config;
    }
}
