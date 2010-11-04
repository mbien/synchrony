package com.synchrony.core;

import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;

/**
 *
 * @author mbien
 */
public class FSFile extends FSNode {

    private byte[] hash = null;

    FSFile(/*FSFolder parent, */Path path, byte[] hash, BasicFileAttributes attrs) {
        super(path);
        this.hash = hash;
    }

    // slow
    private String getHexString(byte[] b) {
        if(b == null) {
            return null;
        }
        String result = "";
        for (int i = 0; i < b.length; i++) {
            result += Integer.toString((b[i] & 0xff) + 0x100, 16).substring(1);
        }
        return result;
    }

    public byte[] getHash() {
        return hash;
    }

    public void setHash(byte[] hash) {
        this.hash = hash;
    }

    @Override
    public String toString() {
        return path.getName() + "[" + getHexString(hash)+ "]";
    }
}
