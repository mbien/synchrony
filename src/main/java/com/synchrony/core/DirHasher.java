package com.synchrony.core;

import com.synchrony.util.RecursiveDirWatcher;
import com.synchrony.util.HashBuilder;
import com.synchrony.util.DirEventListener;
import com.synchrony.util.IOUtils;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.Attributes;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.NoSuchAlgorithmException;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.logging.Level;

import java.util.logging.Logger;
import static java.util.logging.Level.*;
import static java.nio.file.FileVisitResult.*;
import static java.nio.file.LinkOption.*;

/**
 *
 * @author Michael Bien
 */
public class DirHasher implements DirEventListener {

    private static final int MAX_DELETE_BUFFER_SIZE = 512;
    private static final String CHECKSUM_FOLDER = ".equilibrium";

    private final Deque<Path> recentlyDeleted;
    private final HashBuilder hashBuilder;
    private final Path rootDir;
    
    private final Logger log = Logger.getLogger(DirHasher.class.getName());

    private RecursiveDirWatcher watcher;

    public DirHasher(Path dir) throws IOException {

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

    private void initialHashing() {

        log.info("rebuilding hash tree");

        // initial hashing
        SimpleFileVisitor<Path> walker = new SimpleFileVisitor<Path>() {

            @Override
            public FileVisitResult preVisitDirectory(Path dir) {
                if(dir.endsWith(Paths.get(CHECKSUM_FOLDER)))
                    return SKIP_SUBTREE;
                return CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {

                Path dest = resolveHashFileDestination(file);
                try {
                    hashIfNeeded(file, dest);
                } catch (IOException ex) {
                    log.log(SEVERE, "hash file creation was not successful", ex);
                }
                return CONTINUE;
            }

        };

        Files.walkFileTree(rootDir, walker);
    }

    public void processEvents() throws IOException {
        watcher.processEvents();
    }

    public void entryDeleted(Path path) {
        if(recentlyDeleted.size() == MAX_DELETE_BUFFER_SIZE)
            recentlyDeleted.pollLast();
        recentlyDeleted.addFirst(path);
    }

    public void entryCreated(Path dir, Path entry) {

        try {
            if(!entry.endsWith(Paths.get(CHECKSUM_FOLDER))) {

                if (Attributes.readBasicFileAttributes(entry, NOFOLLOW_LINKS).isDirectory()) {

                    System.out.println("folder moved");

                    //visit updated tree
                    SimpleFileVisitor<Path> patchUpdater = new SimpleFileVisitor<Path>() {

                        @Override
                        public FileVisitResult preVisitDirectory(Path dir) {
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

                        @Override
                        public FileVisitResult postVisitDirectory(Path dir, IOException ex) {
                            try {
                                dir.delete();
                            } catch (IOException ex1) {
                            }
                            return CONTINUE;
                        }

                    };

                    Files.walkFileTree(dir, patchUpdater);
                    // FIXME not nesecarily the first
                    recentlyDeleted.pollFirst();

                } else {
                    updateHashFile(entry);
                }

            }
        } catch (IOException ex) {
            Logger.getLogger(DirHasher.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void entryModified(Path path) {
        System.out.println("modified: "+path);
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
                    isDir = Attributes.readBasicFileAttributes(oldHashDest, NOFOLLOW_LINKS).isDirectory();
                }catch(IOException ex) {
                    isDir = false;
                }

                if (isDir) {

                    final IOException[] exception = new IOException[1];
                    
                    SimpleFileVisitor<Path> hashMatcher = new SimpleFileVisitor<Path>() {
                        @Override
                        public FileVisitResult visitFile(Path checkSumFile, BasicFileAttributes attrs) {
                            try {
                                if (hashBuilder.fileEqualsHash(child, checkSumFile)) {
                                    log.info("file was moved: "+ child);
                                    checkSumFile.delete();
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

                    hashIfNeeded(child, newHashDest);
                    break;

                }else{

                    Path oldHashFile = Paths.get(oldHashDest + "." + hashBuilder.HASH_ALGORITHM);
//                    byte[] hash = hashBuilder.buildChecksum(child);

                    if(hashBuilder.fileEqualsHash(child, oldHashFile)) {
                        
                        log.info("file move confirmed: "+deleted+" -> "+child);

                        try {
                            Path newCheckSumFile = Paths.get(newHashDest + "." + hashBuilder.HASH_ALGORITHM);
                            oldHashFile.moveTo(newCheckSumFile, StandardCopyOption.REPLACE_EXISTING);
                        } catch (NoSuchFileException ex) {
                            log.log(WARNING, "can't move hash file: "+oldHashFile);
                            hashBuilder.storeHashFile(child, newHashDest);
                        }

                        it.remove();

                        break;
                    }
                }
            }

            hashIfNeeded(child, newHashDest);

        } catch (IOException ex) {
            log.log(SEVERE, "exception while updating hash file", ex);
            initialHashing();
        }
    }

    private void hashIfNeeded(Path file, Path dest) throws IOException {

        Path hashFile = Paths.get(dest + "." + hashBuilder.HASH_ALGORITHM);
        
        if(hashFile.exists()) {
            if(IOUtils.compareModificationTimes(file, hashFile) > 0)
                hashBuilder.storeHashFile(file, dest);
        }else{
            hashBuilder.storeHashFile(file, dest);
        }
    }

    public boolean checkDirIntegrity(Path dir) {

        final boolean[] result = new boolean[] {true};

        SimpleFileVisitor<Path> checker = new SimpleFileVisitor<Path>() {
            
            @Override
            public FileVisitResult preVisitDirectory(Path dir) {
                if(dir.getName().equals(Paths.get(CHECKSUM_FOLDER))) {
                    return SKIP_SUBTREE;
                }else{
                    return CONTINUE;
                }
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                Path checksumFile = Paths.get(resolveHashFileDestination(file) +"." + hashBuilder.HASH_ALGORITHM);

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


    // for manual testing
    public static void main(String[] args) throws IOException {

        Path dir = Paths.get(System.getProperty("user.home")+"/test");

//        IOUtils.deleteDir(dir.resolve(CHECKSUM_FOLDER));

        DirHasher watchman = new DirHasher(dir);
        watchman.init();
        watchman.processEvents();
    }

}
