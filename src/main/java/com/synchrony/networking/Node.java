/*
 * Created on Tuesday, October 05 2010
 */

package com.synchrony.networking;

import java.util.Objects;

/**
 *
 * @author mbien
 */
public class Node {
    
    private final String name;

    Node(String hostAddress) {
        this.name = hostAddress;

    }

    @Override
    public String toString() {
        return getClass().getName() + "{" + name + "}";
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
