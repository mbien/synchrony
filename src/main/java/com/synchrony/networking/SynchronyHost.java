package com.synchrony.networking;

import com.synchrony.config.Config;
import com.synchrony.core.FSFolder;
import com.synchrony.util.IOUtils;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.StandardProtocolFamily;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousDatagramChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
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

    private final NodeListener nodeListener;
    private final MessageListener msgListener;
    
    private final String multicastAddress;

    private final int multicastSendPort;
    private final int multicastListenPort;
    
    private final int tcpSendPort;
    private final int tcpListenPort;

    // <ID, timestamp>
    private final Map<Node, Long> knownHosts;

    SynchronyHost(Config config, NodeListener nodeListener, MessageListener msgListener) {

        this.multicastAddress = config.multicastaddress;
        
        this.multicastSendPort = config.multicastSendPort;
        this.multicastListenPort = config.multicastListenPort;
        
        this.tcpSendPort = config.tcpSendPort;
        this.tcpListenPort = config.tcpListenPort;
        
        this.knownHosts = new HashMap<>();
        this.nodeListener = nodeListener;
        this.msgListener = msgListener;
    }

    public void startHost() {

        Runnable mcSender = new Runnable() {
            @Override
            public void run() {
                try {
                    startMulticastSender();
                } catch (final InterruptedException | UnknownHostException ex) {
                    throw new RuntimeException(ex);
                }
            }
        };
        
        Runnable mcReceiver = new Runnable() {
            @Override
            public void run() {
                try {
                    startMulticastReceiver();
                } catch (final InterruptedException ex) {
                    throw new RuntimeException(ex);
                }
            }
        };
        
        Runnable tcpServer = new Runnable() {
            @Override
            public void run() {
                try {
                    startTCPServer();
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        };

        new Thread(mcReceiver, "discovery receiver").start();
        new Thread(mcSender, "discovery sender").start();
        
        new Thread(tcpServer, "tcpSender").start();
        
        LOG.info("host started");
    }

    private void startMulticastReceiver() throws InterruptedException {

        byte[] bytes = new byte[MAGIC_STRING.getBytes().length];

        // direct bytebuffer
        ByteBuffer buffer = IOUtils.newDirectByteBuffer(bytes.length);

        while (true) {
            
            AsynchronousDatagramChannel channel = null;
            try{
//                channel = DatagramChannel.open(StandardProtocolFamily.INET);
                channel = AsynchronousDatagramChannel.open(StandardProtocolFamily.INET, null);
                channel.bind(new InetSocketAddress(multicastListenPort));

                Future<SocketAddress> future = channel.receive(buffer); // non blocking

                SocketAddress addr = future.get(DISCOVERY_INTERVAL*2, TimeUnit.SECONDS); // blocking
                InetAddress senderAddress = ((InetSocketAddress) addr).getAddress();

                // get a list of the localhosts IP addresses for filtering
                Set<String> myIPs = getOwnIPs();

                //drop own packages
//                if (!myIPs.contains(senderAddress.getHostAddress())) {

                    buffer.rewind();
                    buffer.get(bytes);

                    synchronized(knownHosts) {
                        Node node = new Node(this, senderAddress.getHostName()+"_"+hashCode(), senderAddress);
                        Long old = knownHosts.put(node, System.currentTimeMillis());
                        if(old == null) {
                            // new host discovered
                            nodeListener.nodeDiscovered(node, new ArrayList<>(knownHosts.keySet()));
                        }
                    }

//                }
            }catch(final IOException | ExecutionException | TimeoutException ex) {
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
        DatagramPacket dgram = new DatagramPacket(bytes, bytes.length, address, multicastSendPort);

        while (true) {
            try(DatagramSocket socket = new DatagramSocket()) {

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
                        nodeListener.nodeLost(node, new ArrayList<>(knownHosts.keySet()));
                    }
                    
                }

                socket.send(dgram);
            }catch(IOException ex) {
                LOG.log(Level.WARNING, "", ex);
            }
            Thread.sleep(DISCOVERY_INTERVAL);
        }
    }

    private void startTCPServer() throws IOException {
        
        ServerSocket ss = new ServerSocket(tcpListenPort);
        
        while(true) {
            try (Socket socket = ss.accept()) {
                try (InputStream is = socket.getInputStream()) {
                    MsgHeader msg = MsgHeader.load(is);
                    
                    LOG.info("received "+msg.toString());
                    msgListener.onMessage(msg, socket);
                }catch(IOException ex){
                    LOG.log(Level.WARNING, "", ex);
                }
            }
        }
        
    }

    FSFolder requestStatus(Node node) throws IOException {
        
        try (Socket socket = new Socket(node.getAddress(), tcpSendPort);
             OutputStream os = socket.getOutputStream()) {
            
            MsgHeader.STATUS_REQUEST.write(os);
            
            // receive
            try (InputStream is = socket.getInputStream();
                 ObjectInputStream ois = new ObjectInputStream(is)) {
                return (FSFolder) ois.readObject();
            } catch (ClassNotFoundException ex) {
                throw new IOException("incompatible protocol", ex);
            }
        }
    }

    void sync(Node node, final Path root, List<String> localNew, List<String> remoteNew) throws IOException {
        LOG.info("syncing...");
        
        final ByteBuffer buffer = IOUtils.newDirectByteBuffer(10000);
        
        if(!localNew.isEmpty()) {
            uploadFiles(node.getAddress(), localNew, root, buffer);
        }
        
        LOG.info("sync done");
        
    }

    private void uploadFiles(final InetAddress address, List<String> files, final Path root, final ByteBuffer buffer) throws IOException {

        for (String local : files) {
            Path path = Paths.get(local);
            
            if(IOUtils.isDirectory(path)) {
                Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes bfa) throws IOException {
                        uploadFile(address, file, root, buffer);
                        return FileVisitResult.CONTINUE;
                    }
                });
            }else{
                uploadFile(address, path, root, buffer);
            }
        }
    }
    
    private void uploadFile(InetAddress address, Path file, Path root, ByteBuffer buffer) throws IOException {
        
        Path relPath = root.relativize(file);
        
        try (SocketChannel sc = SocketChannel.open()) {
            
            sc.connect(new InetSocketAddress(address, tcpSendPort));
            
            OutputStream os = sc.socket().getOutputStream();
            MsgHeader.SYNC_UPLOAD.write(os);
            
            ObjectOutputStream oos = new ObjectOutputStream(os);
            
            try (SeekableByteChannel fc = file.newByteChannel()) {
                oos.writeObject(relPath.toString());
                IOUtils.transfer(fc, sc, buffer);
            }
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
