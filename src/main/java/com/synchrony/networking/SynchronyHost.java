/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.synchrony.networking;

import com.synchrony.config.Config;
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
import java.nio.ByteOrder;
import java.nio.channels.AsynchronousDatagramChannel;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author blip
 * @author mbien
 */
public class SynchronyHost {
    
    private static final Logger LOG = Logger.getLogger(SynchronyHost.class.getName());

    public static final String MAGIC_STRING = "Hello! I'm a Synchrony Host";

    public final long DISCOVERY_INTERVAL = 5000;
    public final long TIME_TILL_DEAD = 2 * DISCOVERY_INTERVAL;

    private final NodeListener listener;
    private final String multicastAddress;

    private final int multicastPort;

    // <ID, timestamp>
    private final Map<Node, Long> knownHosts;

    SynchronyHost(Config config, NodeListener listener) {

        this.multicastAddress = config.multicastaddress;
        this.multicastPort = config.multicastport;
        this.knownHosts = new HashMap<>();
        this.listener = listener;
    }

    public void startHost() {

        Runnable sender = new Runnable() {
            @Override
            public void run() {
                try {
                    startMulticastSender();
                } catch (InterruptedException ex) {
                    throw new RuntimeException(ex);
                } catch (UnknownHostException ex) {
                    throw new RuntimeException("something is seriously wrong", ex);
                }
            }
        };
        
        Runnable receiver = new Runnable() {
            @Override
            public void run() {
                try {
                    startMulticastReceiver();
                } catch (InterruptedException ex) {
                    throw new RuntimeException(ex);
                }
            }
        };

        new Thread(receiver, "receiver").start();
        new Thread(sender, "sender").start();

    }

    private void startMulticastReceiver() throws InterruptedException {

        byte[] bytes = new byte[MAGIC_STRING.getBytes().length];

        // direct bytebuffer (outside heap)
        ByteBuffer buffer = ByteBuffer.allocateDirect(bytes.length).order(ByteOrder.nativeOrder());

        while (true) {

            AsynchronousDatagramChannel channel = null;
            try{
                channel = AsynchronousDatagramChannel.open(StandardProtocolFamily.INET, null);
                channel.bind(new InetSocketAddress(multicastPort));

                Future<SocketAddress> future = channel.receive(buffer); // non blocking

                SocketAddress addr = future.get(DISCOVERY_INTERVAL*2, TimeUnit.SECONDS); // blocking
                InetAddress senderAddress = ((InetSocketAddress) addr).getAddress();

                // get a list of the localhosts IP addresses for filtering
                Set<String> myIPs = getOwnIPs();
                
                //drop own packages
                if (!myIPs.contains(senderAddress.getHostAddress())) {

                    buffer.rewind();
                    buffer.get(bytes);

                    synchronized(knownHosts) {
                        Node node = new Node(senderAddress.getHostName()+"_hash"+hashCode(), senderAddress);
                        Long old = knownHosts.put(node, System.currentTimeMillis());
                        if(old != null) {
                            // new host discovered
                            listener.nodeDiscovered(node, new ArrayList<Node>(knownHosts.keySet()));
                        }
                    }

                }
            }catch(TimeoutException ex) {
                LOG.log(Level.WARNING, "timeout, continuing", ex);
            }catch(IOException ex) {
                LOG.log(Level.WARNING, "continuing", ex);
            }catch(ExecutionException ex) {
                LOG.log(Level.WARNING, "continuing", ex);
            }finally{
                if(channel != null) {
                    try {
                        channel.close();
                    } catch (IOException ex) {
                        LOG.log(Level.WARNING, "continuing", ex);
                    }
                }
            }

        }
    }

    private void startMulticastSender() throws InterruptedException, UnknownHostException {

        byte[] bytes = MAGIC_STRING.getBytes();

        InetAddress address = InetAddress.getByName(multicastAddress);
        DatagramPacket dgram = new DatagramPacket(bytes, bytes.length, address, multicastPort);

        while (true) {
            DatagramSocket socket = null;
            try{
                socket = new DatagramSocket();

                synchronized(knownHosts) {
                    long time = System.currentTimeMillis();
                    List<Node> keysToRemove = new ArrayList<>();
                    for (Map.Entry<Node, Long> entry : knownHosts.entrySet()) {
                        if(time-entry.getValue() > TIME_TILL_DEAD) {
                            keysToRemove.add(entry.getKey());
                        }
                    }
                    for (Node node : keysToRemove) {
                        knownHosts.remove(node);
                        listener.nodeLost(node, new ArrayList<Node>(knownHosts.keySet()));
                    }
                    
                }

                socket.send(dgram);
            }catch(IOException ex) {
                LOG.log(Level.WARNING, "", ex);
            }finally{
                if(socket != null) {
                    socket.close();
                }
            }
            Thread.sleep(DISCOVERY_INTERVAL);
        }
    }

    /**
     * Returns all own IPs.
     */
    private Set<String> getOwnIPs() throws SocketException {

        Set<String> ips = new HashSet<>();

        Enumeration ifaces = NetworkInterface.getNetworkInterfaces();

        while (ifaces.hasMoreElements()) {
            NetworkInterface ni = (NetworkInterface) ifaces.nextElement();

            Enumeration addrs = ni.getInetAddresses();

            while (addrs.hasMoreElements()) {
                InetAddress ia = (InetAddress) addrs.nextElement();
                ips.add(ia.getHostAddress());
            }
        }

        return ips;

    }
}
