package com.synchrony.core;

import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 *
 * @author mbien
 */
public class FSTest {

    private static final Path rootPath = Paths.get("/foo/");

    @Test
    public void test() {

        // /file1
        // /folder1
        // /folder1/subfolder11
        // /folder1/subfolder12
        // /folder2/file2

        FSFolder root = new FSFolder(null, rootPath);

        FSFolder folder1 = createFolder(root, "folder1");
        FSFolder folder2 = createFolder(root, "folder2");
        FSFolder subfolder11 = createFolder(folder1, "subfolder11");
        FSFolder subfolder12 = createFolder(folder1, "subfolder12");

        FSFile file1 = new FSFile(root, rootPath.resolve("file1"), null, null);
        FSFile file2 = new FSFile(folder2, folder2.name.resolve("file2"), null, null);


        root.insert(subfolder11);
        assertEquals(1, root.childs.size());
        root.insert(folder1);
        assertEquals(1, root.childs.size());
        assertEquals(1, ((FSFolder)root.childs.get("folder1")).childs.size());

        root.insert(folder2);
        assertEquals(2, root.childs.size());
        root.insert(subfolder12);
        assertEquals(2, root.childs.size());

        root.insert(file1);
        root.insert(file2);

        System.out.println(root);
        System.out.println(root.childs);
        
        assertEquals(3, root.childs.size());
        assertEquals(2, ((FSFolder)root.childs.get("folder1")).childs.size());
        assertEquals(1, ((FSFolder)root.childs.get("folder2")).childs.size());
        assertEquals(0, subfolder11.childs.size());
        assertEquals(0, subfolder12.childs.size());

        assertTrue(root.childs.containsKey("folder1"));
        assertTrue(root.childs.containsKey("folder2"));
        assertTrue(root.childs.containsKey("file1"));

        assertTrue(((FSFolder)root.childs.get("folder1")).childs.containsKey("subfolder11"));
        assertTrue(((FSFolder)root.childs.get("folder1")).childs.containsKey("subfolder12"));
        assertTrue(((FSFolder)root.childs.get("folder2")).childs.containsKey("file2"));
        
        //remove file
        FSNode removed = root.remove(file1.getPath());
        assertEquals(file1, removed);
        assertNull(root.get(file1.getPath()));
        assertFalse(root.childs.containsKey("file1"));
        
        //remove folder1
        removed = root.remove(folder1.getPath());
        assertEquals(folder1, removed);
        assertNull(root.get(folder1.getPath()));
        assertFalse(root.childs.containsKey("folder1"));
        System.out.println(root);
        
        //remove folder2
        removed = root.remove(folder2.getPath());
        assertEquals(folder2, removed);
        assertNull(root.get(folder2.getPath()));
        assertFalse(root.childs.containsKey("folder2"));
        System.out.println(root);
        

    }

    private FSFolder createFolder(FSFolder path, String name) {
        return new FSFolder(path, path.name.resolve(name));
    }



}