/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.synchrony.mina.sumup;

import com.synchrony.mina.sumup.codec.SumUpProtocolCodecFactory;
import java.net.InetSocketAddress;

import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.serialization.ObjectSerializationCodecFactory;
import org.apache.mina.filter.logging.LoggingFilter;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;

/**
 * (<strong>Entry Point</strong>) Starts SumUp server.
 *
 * @author <a href="http://mina.apache.org">Apache MINA Project</a>
 */
public class Server {
    private static final int SERVER_PORT = 8083;

    // Set this to false to use object serialization instead of custom codec.
    private static final boolean USE_CUSTOM_CODEC = true;

    public static void main(String[] args) throws Throwable {
        NioSocketAcceptor acceptor = new NioSocketAcceptor();

        // Prepare the service configuration.
        if (USE_CUSTOM_CODEC) {
            acceptor.getFilterChain()
                    .addLast(
                            "codec",
                            new ProtocolCodecFilter(
                                    new SumUpProtocolCodecFactory(true)));
        } else {
            acceptor.getFilterChain().addLast(
                    "codec",
                    new ProtocolCodecFilter(
                            new ObjectSerializationCodecFactory()));
        }
        acceptor.getFilterChain().addLast("logger", new LoggingFilter());

        acceptor.setHandler(new ServerSessionHandler());
        acceptor.bind(new InetSocketAddress(SERVER_PORT));

        System.out.println("Listening on port " + SERVER_PORT);
    }
}

