
package com.synchrony.networking;

import com.synchrony.config.Config;
import com.synchrony.core.DirHasher;
import com.synchrony.ui.SynchronyUIManager;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
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
        
        NodeListener listener = new NodeListener() {

            @Override
            public void nodeDiscovered(Node node, List<Node> all) {
                System.out.println("new node "+node);
                System.out.println("all nodes: "+all);
            }

            @Override
            public void nodeLost(Node node, List<Node> all) {
                System.out.println("node died "+node);
                System.out.println("all nodes: "+all);
            }
        };

        Path configFolder = Paths.get(System.getProperty("user.home")).resolve(".synchrony/");
        Path configPath = configFolder.resolve("config.xml");

        Config config = null;
        if(!configFolder.exists() || !configPath.exists()) {
            config = createDefaultConfig(configFolder, config, configPath);
        }else{
            try {
                LOG.info("reading configuration.");
                config = Config.read(configPath.toUri());
            } catch (IOException ex) {
                // TODO maybe a little bit to radical
                LOG.log(Level.WARNING, "can not read configuration, overwriting with default", ex);
                config = createDefaultConfig(configFolder, config, configPath);
            }
        }
        
        SynchronyUIManager ui = new SynchronyUIManager(config);
        ui.init();

        // networking
        SynchronyHost host = new SynchronyHost(config, listener);
        host.startHost();
        
        //TODO we use currently only one folder (the first in the config)
        List<Config.Watcher> watchers = config.watchers;
        if(!watchers.isEmpty()) {
            try {
                DirHasher observer = new DirHasher(Paths.get(watchers.get(0).path));
                observer.processEvents();
            } catch (IOException ex) {
                LOG.log(Level.SEVERE, "can not start dir hasher.", ex);
            }
        }
        
    }
        
    private static Config createDefaultConfig(Path configFolder, Config config, Path configPath) throws RuntimeException {
        try {
            LOG.info("no configuration found, creating default.");
            if (!configFolder.exists()) {
                configFolder.createDirectory();
            }
            config = Config.getDefault();
            config.save(configPath.toUri());
        } catch (IOException ex) {
            throw new RuntimeException("can not create default configuration", ex);
        }
        return config;
    }
}
