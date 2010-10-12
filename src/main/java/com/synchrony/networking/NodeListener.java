/*
 * Created on Tuesday, October 05 2010
 */

package com.synchrony.networking;

import java.util.List;

/**
 *
 * @author mbien
 */
public interface NodeListener {

    public void nodeDiscovered(Node node, List<Node> all);
    
    public void nodeLost(Node node, List<Node> all);

}
