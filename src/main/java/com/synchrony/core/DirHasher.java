package com.synchrony.core;

import java.util.ArrayList;
import com.synchrony.util.RecursiveDirWatcher;
import com.synchrony.util.HashBuilder;
import com.synchrony.util.FSEventListener;
import com.synchrony.util.IOUtils;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.NoSuchAlgorithmException;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;

import java.util.logging.Logger;
import static java.util.logging.Level.*;
import static java.nio.file.FileVisitResult.*;
import static com.synchrony.util.IOUtils.*;

/**
 *
 * @author Michael Bien
 */
public class DirHasher implements FSEventListener {

    private static final int MAX_DELETE_BUFFER_SIZE = 512;
    private static final String CHECKSUM_FOLDER = ".hash";

    private final Deque<Path> recentlyDeleted;
    private final HashBuilder hashBuilder;
    private final Path rootDir;
    
    private static final Logger log = Logger.getLogger(DirHasher.class.getName());

    private RecursiveDirWatcher watcher;

    private final FSFolder root;
    
    private List<FSEventListener> listeners;

    public DirHasher(Path dir) throws IOException {

        root = new FSFolder(null, dir);
        listeners = new ArrayList<>();
        
        System.out.println(root.getFullPath());
        
        recentlyDeleted = new LinkedList<>();
        try {
            hashBuilder = new HashBuilder();
        } catch (NoSuchAlgorithmException ex) {
            throw new RuntimeException(ex);
        }

        rootDir = dir;

        RecursiveDirWatcher.Filter filter = new RecursiveDirWatcher.Filter() {
            public boolean filter(Path path) {
                return !path.getName().toString().equals(CHECKSUM_FOLDER);
            }
        };

        // watch folders
        watcher = new RecursiveDirWatcher(dir, this, filter);
    }

    public void init() throws IOException {
        initialHashing();
        watcher.init();
    }

    private void initialHashing() throws IOException {

        log.info("rebuilding hash tree");

        // initial hashing
        SimpleFileVisitor<Path> walker = new SimpleFileVisitor<Path>() {

            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                if(dir.endsWith(Paths.get(CHECKSUM_FOLDER)))
                    return SKIP_SUBTREE;
                return CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {

                Path dest = resolveHashFileDestination(file);
                try {
                    byte[] hash = hashIfNeeded(file, dest);
                    root.insert(new FSFile(file, hash, attrs));
                } catch (IOException ex) {
                    log.log(SEVERE, "hash file creation was not successful", ex);
                }
                return CONTINUE;
            }

        };

        Files.walkFileTree(rootDir, walker);

        System.out.println(root);
    }

    public void processEvents() throws IOException {
        watcher.processEvents();
    }

    public void entryDeleted(Path path) {
        if(recentlyDeleted.size() == MAX_DELETE_BUFFER_SIZE) {
            recentlyDeleted.pollLast();
        }
        recentlyDeleted.addFirst(path);
        root.remove(path);

        System.out.println(root);
        
        for (FSEventListener listener : listeners) {
            listener.entryDeleted(path);
        }
        
    }

    public void entryCreated(Path dir, Path file) {

        try {
            if(!file.endsWith(Paths.get(CHECKSUM_FOLDER))) {

                if (isDirectory(file)) {

                    System.out.println("folder moved");

                    //visit updated tree
                    SimpleFileVisitor<Path> patchUpdater = new SimpleFileVisitor<Path>() {

                        @Override
                        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                            if(dir.getName().equals(Paths.get(CHECKSUM_FOLDER))) {
                                return SKIP_SUBTREE;
                            }else{
                                return CONTINUE;
                            }
                        }

                        @Override
                        public FileVisitResult visitFile(Path child, BasicFileAttributes attrs) {
                            updateHashFile(child);
                            return CONTINUE;
                        }

                    };

                    Files.walkFileTree(dir, patchUpdater);
                    // FIXME not nesecarily the first
                    recentlyDeleted.pollFirst();

                } else {
                    updateHashFile(file);
                }

            }
            System.out.println(root);
        } catch (IOException ex) {
            Logger.getLogger(DirHasher.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        for (FSEventListener listener : listeners) {
            listener.entryCreated(dir, file);
        }
        
    }

    public void entryModified(Path path) {
        System.out.println("modified: "+path);
        updateHashFile(path);

        System.out.println(root);
        
        for (FSEventListener listener : listeners) {
            listener.entryModified(path);
        }
        
    }

    private void updateHashFile(final Path child) {

        try {

            // build checksum
            Path newHashDest = resolveHashFileDestination(child);

            Iterator<Path> it = recentlyDeleted.iterator();

            while(it.hasNext()) {

                Path deleted = it.next();
                Path oldHashDest = resolveHashFileDestination(deleted);

                boolean isDir = false;
                try{
                    isDir = isDirectory(oldHashDest);
                }catch(IOException ex) {
                    isDir = false;
                }

                if (isDir) {

                    final IOException[] exception = new IOException[1];
                    
                    SimpleFileVisitor<Path> hashMatcher = new SimpleFileVisitor<Path>() {
                        @Override
                        public FileVisitResult visitFile(Path checksumFile, BasicFileAttributes attrs) {
                            try {
                                if (hashBuilder.fileEqualsHash(child, checksumFile)) {
                                    log.info("file was moved: "+ child);
                                    checksumFile.delete();
                                    return TERMINATE;
                                }
                            } catch (IOException ex) {
                                exception[0] = ex;
                                return TERMINATE;
                            }
                            return CONTINUE;
                        }

                    };
                    Files.walkFileTree(oldHashDest, hashMatcher);

                    if(exception[0] != null)
                        throw exception[0];

                    byte[] hash = hashIfNeeded(child, newHashDest);
                    FSNode fsnode = root.get(child);
                    if(fsnode == null) {
                        root.insert(new FSFile(child, hash, null));
                    }else{
                        ((FSFile)fsnode).setHash(hash);
                    }
                    break;

                }else{

                    Path oldHashFile = getHashFileName(oldHashDest);
//                    byte[] hash = hashBuilder.buildChecksum(child);

                    if(hashBuilder.fileEqualsHash(child, oldHashFile)) {
                        
                        log.info("file move confirmed: "+deleted+" -> "+child);

                        try {
                            Path newChecksumFile = getHashFileName(newHashDest);
                            oldHashFile.moveTo(newChecksumFile, StandardCopyOption.REPLACE_EXISTING);
                        } catch (NoSuchFileException ex) {
                            log.log(WARNING, "can't move hash file: "+oldHashFile);
                            hashBuilder.storeHashFile(child, newHashDest);
                        }

                        it.remove();

                        break;
                    }
                }
            }
            
            byte[] hash = hashIfNeeded(child, newHashDest);
            FSNode fsnode = root.get(child);
            if(fsnode == null) {
                root.insert(new FSFile(child, hash, null));
            }else{
                ((FSFile)fsnode).setHash(hash);
            }

        } catch (IOException ex) {
            log.log(SEVERE, "exception while updating hash file", ex);
            try {
                initialHashing();
            } catch (IOException ex1) {
                log.log(SEVERE, "exception while recovering hash tree.", ex1);
            }
        }
    }

    private byte[] hashIfNeeded(Path file, Path dest) throws IOException {

        Path hashFile = getHashFileName(dest);
        
        if(hashFile.exists()) {
            if(IOUtils.compareModificationTimes(file, hashFile) > 0) {
                return hashBuilder.storeHashFile(file, dest);
            }else{
                return hashBuilder.readHashFile(hashFile);
            }
        }else{
            return hashBuilder.storeHashFile(file, dest);
        }
    }

    public boolean checkDirIntegrity(Path dir) throws IOException {

        final boolean[] result = new boolean[] {true};

        SimpleFileVisitor<Path> checker = new SimpleFileVisitor<Path>() {

            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                if(dir.getName().equals(Paths.get(CHECKSUM_FOLDER))) {
                    return SKIP_SUBTREE;
                }else{
                    return CONTINUE;
                }
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                Path checksumFile = getHashFileName(resolveHashFileDestination(file));

                if(checksumFile.notExists()) {
                    log.warning("checksum for "+file+" does not exist");
                    result[0] = false;
                    return TERMINATE;
                }

                return CONTINUE;
            }


        };
        Files.walkFileTree(dir, checker);

        return result[0];
    }

    private Path resolveHashFileDestination(Path path) {
        Path dest = path.subpath(rootDir.getNameCount(), path.getNameCount());
        return rootDir.resolve(CHECKSUM_FOLDER).resolve(dest);
    }
    
    public void addFSListener(FSEventListener listener) {
        listeners.add(listener);
    }
    
    public boolean removeFSListener(FSEventListener listener) {
        return listeners.remove(listener);
    }

    private Path getHashFileName(Path dest) {
        return Paths.get(dest + "." + hashBuilder.HASH_ALGORITHM);
    }
    
    public FSFolder getSnapshot() {
        return root.clone();
    }
    
    public Path getRootDir() {
        return rootDir;
    }

    // for manual testing
    public static void main(String[] args) throws IOException {

        Path dir = Paths.get(System.getProperty("user.home")+"/test");

//        IOUtils.deleteDir(dir.resolve(CHECKSUM_FOLDER));

        DirHasher watchman = new DirHasher(dir);
        watchman.init();
        watchman.processEvents();
    }

}
