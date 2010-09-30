
package com.synchrony.networking;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import static com.synchrony.networking.SynchronyHost.HostType.*;

/**
 *
 * @author blip
 */
public class SynchronyHostStarter {

    public static void main(String[] args) {

        String hostID = args[0];
        String mcAdress = args[1];
        int multicastPort = Integer.parseInt(args[2]);

        Map<String, Long> knownHosts = new HashMap<String, Long>();

        // multicast sender to distribute lookups for possible synchrony hosts
        SynchronyHost multicastSender = new SynchronyHost(MulticastReceiver, hostID, 27, mcAdress, multicastPort, knownHosts);
        // multicast receiver to receive and answer lookups from other hosts
        SynchronyHost multicastReceiver = new SynchronyHost(MulticastSender, hostID, 27, mcAdress, multicastPort, knownHosts);

        multicastReceiver.start();
        multicastSender.start();

        Map<String, Long> hostSnapshot = new HashMap<String, Long>();

        while (true) {

            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                Logger.getLogger(SynchronyHostStarter.class.getName()).log(Level.SEVERE, null, ex);
            }

            //get a snapshot of all known hosts at this time
            synchronized(knownHosts) {
                hostSnapshot = new HashMap<String, Long>(knownHosts);
            }

            System.out.println("\t SynchronyHostStarter knows this hosts: " + hostSnapshot);

            hostSnapshot.clear();
        }


    }
}
