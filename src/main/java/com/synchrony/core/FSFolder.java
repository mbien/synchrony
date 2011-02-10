package com.synchrony.core;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;

/**
 *
 * @author mbien
 */
public class FSFolder extends FSNode {

    //<name, node>
    HashMap<String, FSNode> childs;

    public FSFolder(FSFolder parent, Path path) {
        super(parent, path);
        this.childs = new LinkedHashMap<>();
    }

    void insert(FSNode node) {
        Path relative = name.relativize(node.name);

        Path segment = null;
        FSFolder current = this;
        int i = 0;
        for (Iterator<Path> names = relative.iterator(); names.hasNext();) {
            i++;

            segment = names.next();
            FSNode child = current.childs.get(segment.toString());

            if(child == null) {
                if(names.hasNext()) {
                    FSFolder folder = new FSFolder(current, current.name.resolve(segment));
                    current.childs.put(segment.toString(), folder);
                    current = folder;
                }
            }else{
                current = (FSFolder) child;
            }
        }
        
        //already added?
        if(!current.equals(node)) {
            current.childs.put(segment.toString(), node);
            node.parent = current;
        }

    }

    FSNode get(Path node) {
        Path relative = name.relativize(node);
        Iterator<Path> names = relative.iterator();
        return getImpl(names, node, false);
    }

    FSNode remove(Path node) {
        Path relative = name.relativize(node);
        Iterator<Path> names = relative.iterator();
        return getImpl(names, node, true);
    }

    private FSNode getImpl(Iterator<Path> names, Path node, boolean remove) {
        String segment = names.next().toString();
        FSNode child = childs.get(segment);
        
        if(remove && child != null) {
            childs.remove(segment);
        }

        if(child == null) {
            return null;
        }else if(names.hasNext() && child instanceof FSFolder) {
            return ((FSFolder)child).getImpl(names, node, remove);
        }else{
            return child;
        }

    }

    public List<String> substract(FSFolder otherFolder) {
        return substract(otherFolder, null);
    }
    
    public List<String> substract(FSFolder otherFolder, List<String> list) {
        
        if(list == null) {
            list = new ArrayList<>();
        }
        
        for (Entry<String, FSNode> child : childs.entrySet()) {
            
            boolean found = false;
            FSNode node = child.getValue();
            FSNode otherNode = null;
            for (Entry<String, FSNode> otherChild : otherFolder.childs.entrySet()) {
                otherNode = otherChild.getValue();
                if(otherNode.getName().equals(node.getName())) {
                    if(otherNode instanceof FSFile && node instanceof FSFile){
                        if(Arrays.equals(((FSFile)otherNode).getHash(), ((FSFile)node).getHash())) {
                            found = true;
                            break;
                        }
                    }else{
                        found = true;
                        break;
                    }
                        
                }
            }
            
            // local node is new (file or folder)
            if(!found) {
                list.add(child.getValue().getFullPath());
            // check folders
            }else if(node instanceof FSFolder){
                if(otherNode instanceof FSFolder) {
                    list.addAll(((FSFolder)node).substract(((FSFolder)otherNode)));
                }else{
                    // TODO folder vs file conflict
                }
            }
            
        }
        
        return list;
    }
        

    @Override
    public String toString() {
        String str = name.toString()+childs;
//        for (FSNode node : childs.values()) {
//            str += "\n"+node;
//        }
        return str;
    }

    @Override
    public FSFolder clone() {
        FSFolder clone = new FSFolder(null, name);
        for (FSNode node : childs.values()) {
            clone.insert((FSNode)node.clone());
        }
        return clone;
    }
    
}