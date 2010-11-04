package com.synchrony.core;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;

/**
 *
 * @author mbien
 */
public class FSFolder extends FSNode {

    HashMap<Path, FSNode> childs;

    public FSFolder(/*FSFolder parent, */Path path) {
        super(path);
        this.childs = new LinkedHashMap<>();
    }

    void insert(FSNode node) {
        Path relative = path.relativize(node.path);

        Path name = null;
        FSFolder current = this;
        int i = 0;
        for (Iterator<Path> names = relative.iterator(); names.hasNext();) {
            i++;

            name = names.next();
            FSNode child = current.childs.get(name);

            if(child == null) {
                if(names.hasNext()) {
                    FSFolder folder = new FSFolder(current.path.resolve(name));
                    current.childs.put(name, folder);
                    current = folder;
                }
            }else{
                current = (FSFolder) child;
            }
        }
        
        //already added?
        if(!current.equals(node)) {
            current.childs.put(name, node);
        }

    }

    FSNode get(Path node) {
        Path relative = path.relativize(node);
        Iterator<Path> names = relative.iterator();
        return getImpl(names, node);
    }

    private FSNode getImpl(Iterator<Path> names, Path node) {
        Path name = names.next();
        FSNode child = childs.get(name);

        if(child == null) {
            return null;
        }else if(names.hasNext() && child instanceof FSFolder) {
            return ((FSFolder)child).getImpl(names, node);
        }else{
            return child;
        }

    }

    void remove(Path path) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public String toString() {
        String str = path.toString()+childs;
//        for (FSNode node : childs.values()) {
//            str += "\n"+node;
//        }
        return str;
    }

}
