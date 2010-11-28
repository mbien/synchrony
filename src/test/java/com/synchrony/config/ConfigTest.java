package com.synchrony.config;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 *
 * @author mbien
 */
public class ConfigTest {

    @Test
    public void readConfig() throws IOException, URISyntaxException {
        
        URI stream = getClass().getResource("testconfig.xml").toURI();
        assertNotNull("config not found", stream);
        
        Config config = Config.read(stream);

        assertNotNull(config);

        assertEquals(config.multicastport, 5001);
        assertEquals(config.unicastport, 5001);
        assertEquals(config.heartbeat, 5000);

        List<Config.Watcher> watchers = config.watchers;
        assertNotNull(watchers);
        assertFalse(watchers.isEmpty());

        for (Config.Watcher watcher : watchers) {
            assertNotNull(watcher.name);
            assertNotNull(watcher.path);
            assertTrue(watcher.enabled);
        }

    }

//    @Test
    // TODO write test
    public void writeConfig() {

    }

}
