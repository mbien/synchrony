/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.synchrony.networking;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.StandardProtocolFamily;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousDatagramChannel;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author blip
 */
public class SynchronyHost extends Thread {

    public static final String MAGIC_STRING = "Hello! I'm a Synchrony Host";

    public static final long DISCOVERY_INTERVAL = 5000;
    public static final long TIME_TILL_DEAD = 2 * DISCOVERY_INTERVAL;

    enum HostType {MulticastSender, MulticastReceiver, UnicastReceiver}

    private HostType hostType;

    private String hostID;
    private int bufferLength;
    private String multicastAddress;

    private int multicastPort;
    private int unicastPort;
    private final Map<String, Long> knownHosts;

    SynchronyHost(
            HostType hostType, String hostID, int bufferLength,
            String multicastAddress, int multicastPort,
            int unicastPort, Map<String, Long> knownHosts) {

        this.hostType = hostType;
        this.hostID = hostID;
        this.bufferLength = bufferLength;
        this.multicastAddress = multicastAddress;
        this.multicastPort = multicastPort;
        this.unicastPort = unicastPort;
        this.knownHosts = knownHosts;

    }

    public void startHost() throws IOException, InterruptedException {

        if (hostType == HostType.MulticastSender) {
            startMulticastSender();
        } else if (hostType == HostType.MulticastReceiver) {//hostType = 1 means a multicast receiver @ port 4711
            try {
                startMulticastReceiver();
            } catch (SocketException ex) {
                Logger.getLogger(SynchronyHost.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ExecutionException ex) {
                Logger.getLogger(SynchronyHost.class.getName()).log(Level.SEVERE, null, ex);
            } catch (TimeoutException ex) {
                Logger.getLogger(SynchronyHost.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else if (hostType == HostType.UnicastReceiver) {
            startUnicastReceiver();
        }

    }

    private void startUnicastReceiver() throws IOException, SocketException {
        byte[] b = new byte[bufferLength];
        DatagramPacket dgram = new DatagramPacket(b, b.length);
        while (true) {
            DatagramSocket recvSocket = new DatagramSocket(unicastPort);
            recvSocket.receive(dgram); // blocks until a datagram is received
            if (new String(dgram.getData()).equals(MAGIC_STRING)) {
                System.out.println("[Host " + hostID + "] UCR got a correct answer: " + dgram.getAddress() + ':' + dgram.getPort() + " is a valid remote host");
                String host = dgram.getAddress().getHostAddress();
                synchronized(knownHosts) {
                    knownHosts.put(host, System.currentTimeMillis());
                }
            }
            recvSocket.close();
            System.err.println("[Host " + hostID + "] Currently known synchrony hosts: " + knownHosts);
        }
    }

    private void startMulticastReceiver() throws IOException, SocketException, InterruptedException, ExecutionException, TimeoutException {

        ArrayList<String> myIPs = null;
        byte[] bytes = new byte[bufferLength];
        ByteBuffer buffer = ByteBuffer.allocateDirect(bufferLength);

        while (true) {
            
            // get a list of the localhosts IP addresses for filtering
            myIPs = getOwnIPs();

            AsynchronousDatagramChannel channel = AsynchronousDatagramChannel.open(StandardProtocolFamily.INET, null);
            channel.bind(new InetSocketAddress(multicastPort));

            Future<SocketAddress> future = channel.receive(buffer); // non blocking

            try{
                SocketAddress addr = future.get(DISCOVERY_INTERVAL*2, TimeUnit.SECONDS); // blocking
                InetAddress senderAddress = ((InetSocketAddress) addr).getAddress();

                //drop own packages
                if (!myIPs.contains(senderAddress.getHostAddress())) {

                    buffer.rewind();
                    buffer.get(bytes);
                    System.out.println("[Host " + hostID + "] MCR received " + bytes.length + " bytes (\"" + new String(bytes) + "\") from " + senderAddress.getHostAddress());

                    // send
                    bytes = MAGIC_STRING.getBytes();
                    DatagramPacket dgram = new DatagramPacket(bytes, bytes.length, senderAddress, unicastPort);
                    DatagramSocket sendSocket = new DatagramSocket();
                    sendSocket.send(dgram);
                    sendSocket.close();
                } else {
                    //   System.out.println(hostID + ": Received a package from myself, boring!");
                }
            }catch(TimeoutException ex) {
                Logger.getLogger(getClass().getName()).log(Level.WARNING, "timeout", ex);
            }finally{
                channel.close();
            }

        }
    }

    private void startMulticastSender() throws SocketException, UnknownHostException, IOException, InterruptedException {
        //byte[] b = new byte[bufferLength];
        byte[] b = MAGIC_STRING.getBytes();
        InetAddress address = InetAddress.getByName(multicastAddress);
        DatagramPacket dgram = new DatagramPacket(b, b.length, address, multicastPort);
        while (true) {
            DatagramSocket socket = new DatagramSocket();

            synchronized(knownHosts) {
                long time = System.currentTimeMillis();
                List<String> keysToRemove = new ArrayList<String>();
                for (Map.Entry<String, Long> entry : knownHosts.entrySet()) {
                    if(time-entry.getValue() > TIME_TILL_DEAD) {
                        keysToRemove.add(entry.getKey());
                    }
                }
                for (String key : keysToRemove) {
                    knownHosts.remove(key);
                }
            }

            //  System.err.print(".");
            System.out.println("[Host " + hostID + "] MCS sent " + b.length + " bytes (\"" + new String(b) + "\") to " + dgram.getAddress() + ':' + dgram.getPort());
            socket.send(dgram);
            Thread.sleep(DISCOVERY_INTERVAL);
        }
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
