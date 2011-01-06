/*
 * Created on Tuesday, October 05 2010
 */

package com.synchrony.networking;

import com.synchrony.core.FSFolder;
import java.io.IOException;
import java.net.InetAddress;
import java.util.List;
import java.util.Objects;

/**
 *
 * @author mbien
 */
public class Node {
    
    private final String name;
    
    private final InetAddress address;
    private final SynchronyHost host;


    Node(SynchronyHost host, String hostAddress, InetAddress address) {
        this.name = hostAddress;
        this.address = address;
        this.host = host;
    }

    void initialSync(FSFolder localSnapshot) throws IOException {
        FSFolder remoteSnapshot = host.requestStatus(this);
        
        List<String> localNew = localSnapshot.substract(remoteSnapshot);
        List<String> remoteNew = remoteSnapshot.substract(localSnapshot);
        
        System.out.println("local new:  "+ localNew);
        System.out.println("remote new: "+ remoteNew);
        host.sync(this, localSnapshot.getPath(), localNew, remoteNew);
        
    }

    public InetAddress getAddress() {
        return address;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" + name+"["+address.getHostAddress()+"]" + "}";
    }


    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Node other = (Node) obj;
        if (!Objects.equals(this.name, other.name)) {
            return false;
        }
        return true;
    }

    public int hashCode() {
        int hash = 7;
        hash = 71 * hash + Objects.hashCode(this.name);
        return hash;
    }
    


}
