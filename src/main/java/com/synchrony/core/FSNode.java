package com.synchrony.core;

import java.io.Serializable;
import java.nio.file.Path;

/**
 * In-memory representation of a file/folder.
 * @author mbien
 */
public abstract class FSNode implements Serializable {

    protected final Path path;
//    private final FSFolder parent;

    FSNode(/*FSFolder parent, */Path path) {
        this.path = path;
//        this.parent = parent;
    }

    public Path getPath() {
        return path;
    }


//    @Override
//    public int hashCode() {
//        return path.hashCode();
//    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof FSNode) {
            return ((FSNode) obj).path.equals(path);
        }
        return false;
    }
}
