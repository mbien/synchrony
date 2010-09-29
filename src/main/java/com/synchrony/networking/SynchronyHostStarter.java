/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.synchrony.networking;

import java.util.ArrayList;
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
        int unicastPort = Integer.parseInt(args[3]);

        ArrayList<String> knownHosts = new ArrayList<String>(10);
        ArrayList<String> hosts = new ArrayList<String>(101);

        // multicast sender to distribute lookups for possible synchrony hosts
        SynchronyHost multicastSender = new SynchronyHost(MulticastReceiver, hostID, 27, mcAdress, multicastPort, unicastPort, knownHosts);
        // multicast receiver to receive and answer lookups from other hosts
        SynchronyHost multicastReceiver = new SynchronyHost(MulticastSender, hostID, 27, mcAdress, multicastPort, unicastPort, knownHosts);
        // unicast receiver to receive unicast packages on a special port to determine a host to be a synchrony host
        SynchronyHost unicastReceiver = new SynchronyHost(UnicastReceiver, hostID, 27, mcAdress, multicastPort, unicastPort, knownHosts);

        unicastReceiver.start();
        multicastReceiver.start();
        multicastSender.start();

        while (true) {

            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                Logger.getLogger(SynchronyHostStarter.class.getName()).log(Level.SEVERE, null, ex);
            }

            //get a snapshot of all known hosts at this time
            hosts.addAll(knownHosts);

//            System.out.println(hosts.size());

            for (String host : hosts) {
                // now we can inform all peers about changes (create, modify, delete, move)
                System.out.println("\t SynchronyHostStarter knows this hosts: " + hosts);
            }

            hosts.clear();
        }


    }
}
