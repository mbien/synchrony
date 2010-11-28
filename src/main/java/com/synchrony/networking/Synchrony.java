
package com.synchrony.networking;

import com.synchrony.config.Config;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author blip
 * @author mbien
 */
public class SynchronyHostStarter {

    private static final Logger LOG = Logger.getLogger(SynchronyHostStarter.class.getName());

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

        // multicast sender to distribute lookups for possible synchrony hosts
        SynchronyHost host = new SynchronyHost(config, listener);

        host.startHost();
        LOG.info("started");
        
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
