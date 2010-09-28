/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.synchrony.networking;

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

        // multicast sender to distribute lookups for possible synchrony hosts
        SynchronyHost multicastSender = new SynchronyHost(0, hostID, 32, mcAdress, multicastPort, unicastPort);
        // multicast receiver to receive and answer lookups from other hosts
        SynchronyHost multicastReceiver = new SynchronyHost(1, hostID, 32, mcAdress, multicastPort, unicastPort);
        // unicast receiver to receive unicast packages on a special port to determine a host to be a synchrony host
        SynchronyHost unicastReceiver = new SynchronyHost(2, hostID, 32, mcAdress, multicastPort, unicastPort);

        unicastReceiver.start();
        multicastReceiver.start();
        multicastSender.start();





    }
}
