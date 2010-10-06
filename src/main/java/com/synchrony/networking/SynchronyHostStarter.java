
package com.synchrony.networking;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author blip
 */
public class SynchronyHostStarter {

    public static void main(String[] args) {

        NodeListener listener = new NodeListener() {

            @Override
            public void nodeDiscovered(Node node, List<Node> all) {
                System.out.println("new node "+node);
                System.out.println("all nodes: "+all);
            }

            @Override
            public void nodeLost(Node node, List<Node> all) {
                System.out.println("node died "+node);
                System.out.println("all nodes: "+all);
            }
        };

        // multicast sender to distribute lookups for possible synchrony hosts
        SynchronyHost host = new SynchronyHost(listener);

        host.startHost();

    }
}
