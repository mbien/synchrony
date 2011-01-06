package com.synchrony.core;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * In-memory representation of a file/folder.
 * @author mbien
 */
public abstract class FSNode implements Serializable, Cloneable {

    /**
     * Absolute path.
     */
    protected transient Path name;
    protected FSFolder parent;

    FSNode(FSFolder parent, Path path) {
        this.name = path;
        this.parent = parent;
    }

    public Path getPath() {
        return name;
    }
    
    public String getFullPath() {
        
        StringBuilder sb = new StringBuilder();
//        if(!name.isAbsolute()) {
//            sb.append('/');
//        }
        sb.append(name);
        
//        FSFolder folder = this.parent;
//        while(folder != null) {
//            sb.insert(0, folder.getName()).insert(0, '/');
//            folder = folder.parent;
//        }
        
        return sb.toString();
    }
    
    public String getName() {
        return name.getName().toString();
    }


//    @Override
//    public int hashCode() {
//        return path.hashCode();
//    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof FSNode) {
            return ((FSNode) obj).name.equals(name);
        }
        return false;
    }

    @Override
    public abstract FSNode clone();
    
    private void writeObject(ObjectOutputStream stream) throws IOException {
        stream.defaultWriteObject();
        stream.writeObject(name.toString());
    }

    private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
        String p = (String) stream.readObject();
        name = Paths.get(p);
    }

    
    
}
