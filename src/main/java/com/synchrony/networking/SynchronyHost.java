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
    private ArrayList<String> knownHosts;

    public void startHost() throws IOException, InterruptedException {

        //hostType = 0 means a multicast sender
        if (hostType == 0) {
            DatagramSocket socket = new DatagramSocket();

            //byte[] b = new byte[bufferLength];
            byte[] b = "Hello! I'm a Synchrony Host".getBytes();


            DatagramPacket dgram;

            dgram = new DatagramPacket(b, b.length, InetAddress.getByName(multicastAddress), multicastPort);

            while (true) {
                knownHosts.clear();
                //  System.err.print(".");
                System.out.println("[Host " + hostID + "] MCS sent " + b.length + " bytes (\"" + new String(b) + "\") to "
                        + dgram.getAddress() + ':' + dgram.getPort());
                socket.send(dgram);
                Thread.sleep(10000);
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
                if (myIPs.contains(dgram.getAddress().getHostAddress())) {
                    System.out.println("[Host " + hostID + "] MCR received " + dgram.getLength()
                            + " bytes (\"" + new String(dgram.getData()) + "\") from " + dgram.getAddress() + ':' + dgram.getPort());
                    b = "Hello! I'm a Synchrony Host".getBytes();
                    dgram = new DatagramPacket(b, b.length, dgram.getAddress(), unicastPort);
                    sendSocket.send(dgram);
                } else {
                    //   System.out.println(hostID + ": Received a package from myself, boring!");
                }
            }

        } else if (hostType == 2) {//hostType = 2 means a unicast receiver @ port 5000
            byte[] b = new byte[bufferLength];
            DatagramPacket dgram = new DatagramPacket(b, b.length);
            DatagramSocket recvSocket = new DatagramSocket(unicastPort);
            while (true) {
                recvSocket.receive(dgram); // blocks until a datagram is received
                if ((new String(dgram.getData()).equals("Hello! I'm a Synchrony Host"))) {
                    System.out.println("[Host " + hostID + "] UCR got a correct answer: "
                            + dgram.getAddress() + ':' + dgram.getPort() + " is a valid remote host");
                    String host = dgram.getAddress().getHostAddress() + ":" + dgram.getPort();

                    if (!knownHosts.contains((String)host)) {
                        knownHosts.add(host);
                    }

                }
                System.err.println("[Host " + hostID + "] Currently known synchrony hosts: " + knownHosts);
            }
        } else {
            throw new RuntimeException("Invalid hostType given: " + hostType);
        }
    }

    public SynchronyHost(
            int hostType, String hostID, int bufferLength,
            String multicastAddress, int multicastPort,
            int unicastPort, ArrayList<String> knownHosts) {

        this.hostType = hostType;
        this.hostID = hostID;
        this.bufferLength = bufferLength;
        this.multicastAddress = multicastAddress;
        this.multicastPort = multicastPort;
        this.unicastPort = unicastPort;
        this.knownHosts = knownHosts;

    }

    public static void main(String[] args) {
//
//        int bufferLength = 32;
//        String mcastAddr = "224.0.0.1";
//
//
//        int destPort = 4711;
//
//        SynchronyHost sender = new SynchronyHost(0, "A", bufferLength, mcastAddr, destPort, 5000);
//        SynchronyHost receiver = new SynchronyHost(1, "A", bufferLength, mcastAddr, destPort, 5000);
//
//        receiver.start();
//        sender.start();
//
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
