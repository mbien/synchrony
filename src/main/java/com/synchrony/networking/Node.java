/*
 * Created on Tuesday, October 05 2010
 */

package com.synchrony.networking;

import java.net.InetAddress;
import java.util.Objects;

/**
 *
 * @author mbien
 */
public class Node {
    
    private final String name;

    private byte[] sessionKeyReceive = null;

    private  byte[] sessionKeySend = null;
    
    private final InetAddress address;


    Node(String hostAddress, InetAddress address) {
        this.name = hostAddress;
        this.address = address;
    }

    public void setSessionKeyReceive(byte[] key) {
        this.sessionKeyReceive = key;
    }
    
    public void setSessionKeySend(byte[] key) {
        this.sessionKeySend = key;
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
