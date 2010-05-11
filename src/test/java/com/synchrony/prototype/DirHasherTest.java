package com.synchrony.prototype;

import com.synchrony.prototype.util.IOUtils;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Random;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

import static junit.framework.Assert.*;

/**
 *
 * @author Michael Bien
 */
public class DirHasherTest {

    public static final Path root = Paths.get(System.getProperty("user.home")+"/test");
    private static final int ONE_MB = 1048576;

    private static final FileAttribute<Set<PosixFilePermission>> attribs =
            PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rwxr-x---"));

    private static DirHasher dirWatcher;

    private static Path file1;
    private static Path file2;
    private static Path file3;

    private static Path folder1;
    private static Path folder2;

    @BeforeClass
    public static void setUp() throws IOException {

        folder1 = root.resolve("level1");
        folder2 = root.resolve("level12");

        file1 = folder1.resolve("file1.x");
        file2 = folder1.resolve("file2.x");
        file3 = folder2.resolve("file3.x");

        try{
            root.createDirectory(attribs);
        }catch(FileAlreadyExistsException ex) {
            System.out.println("dir already there");
        }

        IOUtils.deleteFilesInDir(root);

        dirWatcher = new DirHasher(root);
    }

    @After
    public void tearDown() {
    }


//    @Test
//    public void testCreateFiles() throws IOException {
//    }


    /**
     * Test of processEvents method, of class RecursiveDirWatcher.
     */
    @Test
    public void testProcessEvents() throws IOException {

        System.out.println("processEvents");

        createFile(file1, ONE_MB  );
        createFile(file2, ONE_MB*2);
        createFile(file3, ONE_MB*3);
        
        final DirHasher w = dirWatcher;
        w.init();

        check();

        new Thread() {
            @Override
            public void run() {
                try {
                    w.processEvents();
                } catch (IOException ex) {
                    Logger.getLogger(DirHasherTest.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

        }.start();

    }

    @Test
    public void testMovementDelayed() throws IOException, InterruptedException {
        System.out.println("move test");

        file1 = move(file1, folder2);
        Thread.sleep(1000);   check();
        
        file2 = move(file2, folder2);
        Thread.sleep(1000);   check();

        file3 = move(file3, folder1);
        Thread.sleep(1000);   check();

        folder1 = move(folder1, folder2);
        Thread.sleep(1000);   check();

        folder1 = move(folder1, root);
        Thread.sleep(1000);   check();

        folder2 = move(folder2, folder1);
        Thread.sleep(1000);   check();


//        synchronized(this) {
//            try {
//                wait();
//            } catch (InterruptedException ex) {
//                Logger.getLogger(RecursiveDirWatcherTest.class.getName()).log(Level.SEVERE, null, ex);
//            }
//        }
    }

    @Test
    public void testFileCreation() throws IOException {
        System.out.println("create filest test");
        createFile(root.resolve("new/nfile1.x"), ONE_MB*10);
        createFile(root.resolve("new/nfile2.x"), ONE_MB*10);
        createFile(root.resolve("new/nfile3.x"), ONE_MB*10);
    }

    private static Path move(Path src, Path dest) throws IOException {
        dest = dest.resolve(src.getName());
        System.out.println("moving: "+src +" -> "+dest);
        return src.moveTo(dest, StandardCopyOption.REPLACE_EXISTING);
    }

    private static void createFile(Path file, int size) throws IOException {
        Files.createDirectories(file.getParent(), attribs);
        OutputStream os = file.newOutputStream(StandardOpenOption.CREATE);
        writeRandomn(os, size);
        os.close();
    }


    private static void writeRandomn(OutputStream os, int numBytes) throws IOException{
        Random rnd = new Random();
        byte[] bytes = new byte[numBytes];
        rnd.nextBytes(bytes);
        os.write(bytes);
    }

    private void check() {
        System.out.print("checking...");
        assertTrue(dirWatcher.checkDirIntegrity(root));
        System.out.println("done");
    }


}