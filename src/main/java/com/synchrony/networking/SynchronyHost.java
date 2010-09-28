/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.synchrony.networking;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author blip
 */
public class SynchronyHost extends Thread {

    private int hostType;
    private String hostID;
    private int bufferLength;
    private String multicastAddress;
    private int multicastPort;
    private int unicastPort;
    private List<String> knownHosts = new ArrayList<String>();

    public void startHost() throws IOException, InterruptedException {

        //hostType = 0 means a multicast sender
        if (hostType == 0) {
            DatagramSocket socket = new DatagramSocket();

            byte[] b = new byte[bufferLength];
            DatagramPacket dgram;

            dgram = new DatagramPacket(b, b.length, InetAddress.getByName(multicastAddress), multicastPort);

            while (true) {
                resetKnownHosts();
                //  System.err.print(".");
//                System.err.println(hostID + "(MulticastSender): Sending " + b.length + " bytes to "
//                        + dgram.getAddress() + ':' + dgram.getPort());
                socket.send(dgram);
                Thread.sleep(5000);

            }

        } else if (hostType == 1) {//hostType = 1 means a multicast receiver @ port 4711

            //System.out.println(hostID + ": my IP is " + InetAddress.getLocalHost().getHostAddress().toString());
            ArrayList<String> myIPs = null;

            DatagramSocket sendSocket = new DatagramSocket();

            byte[] b = new byte[bufferLength];
            DatagramPacket dgram = new DatagramPacket(b, b.length);

            MulticastSocket recvSocket = new MulticastSocket(multicastPort); // must bind receive side
            recvSocket.joinGroup(InetAddress.getByName(multicastAddress));

            while (true) {

                recvSocket.receive(dgram); // blocks until a datagram is received
                // get a list of the localhosts IP addresses for filtering
                myIPs = getOwnIPs();
                //drop own packages
                if (!myIPs.contains(dgram.getAddress().getHostAddress())) {
//                System.err.println(hostID + "(MulticastReceiver): Received " + dgram.getLength()
//                        + " bytes from " + dgram.getAddress() + ':' + dgram.getPort());
                    // now we know the ip of the other host: let's answer him via an unicast packet
                    dgram = new DatagramPacket(b, b.length, dgram.getAddress(), unicastPort);
                    sendSocket.send(dgram);
                } else {
//                    System.out.println(hostID + ": Received a package from myself, boring!");
                }
            }

        } else if (hostType == 2) {//hostType = 2 means a unicast receiver @ port 5000
            byte[] b = new byte[bufferLength];
            DatagramPacket dgram = new DatagramPacket(b, b.length);
            DatagramSocket recvSocket = new DatagramSocket(unicastPort);
            while (true) {
                recvSocket.receive(dgram); // blocks until a datagram is received
//                System.err.println(hostID + "(UnicastReceiver): "
//                         + dgram.getAddress() + ':' + dgram.getPort() + " is a valid remote host");
                String host = dgram.getAddress().getHostAddress() + ":" + dgram.getPort();
                knownHosts.remove(host);
                knownHosts.add(host);
                System.out.println("[Host " + hostID +"] Currently known synchrony hosts: " + knownHosts);
            }
        } else {
            throw new RuntimeException("Invalid hostType given: " + hostType);
        }
    }

    private synchronized void resetKnownHosts() {
        knownHosts.clear();
    }

    public SynchronyHost(
            int hostType, String hostID, int bufferLength, String multicastAddress, int multicastPort, int unicastPort) {

        this.hostType = hostType;
        this.hostID = hostID;
        this.bufferLength = bufferLength;
        this.multicastAddress = multicastAddress;
        this.multicastPort = multicastPort;
        this.unicastPort = unicastPort;

    }

    public static void main(String[] args) {

        int bufferLength = 32;
        String mcastAddr = "224.0.0.1";


        int destPort = 4711;

        SynchronyHost sender = new SynchronyHost(0, "A", bufferLength, mcastAddr, destPort, 5000);
        SynchronyHost receiver = new SynchronyHost(1, "A", bufferLength, mcastAddr, destPort, 5000);

        receiver.start();
        sender.start();


    }

    @Override
    public void run() {
        try {
            startHost();

        } catch (IOException ex) {
            Logger.getLogger(SynchronyHost.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(SynchronyHost.class.getName()).log(Level.SEVERE, null, ex);
        }


    }

    private ArrayList<String> getOwnIPs() throws SocketException {

        ArrayList<String> ips = new ArrayList<String>();

        Enumeration ifaces = NetworkInterface.getNetworkInterfaces();

        while (ifaces.hasMoreElements()) {
            NetworkInterface ni = (NetworkInterface) ifaces.nextElement();
            //System.out.println(ni.getName() + ":");

            Enumeration addrs = ni.getInetAddresses();

            while (addrs.hasMoreElements()) {
                InetAddress ia = (InetAddress) addrs.nextElement();
                // System.out.println(" " + ia.getHostAddress());
                ips.add(ia.getHostAddress());


            }
        }

        //System.out.println(ips);
        return ips;

    }
}
