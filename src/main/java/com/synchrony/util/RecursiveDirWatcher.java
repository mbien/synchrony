package com.synchrony.util;

import java.io.IOError;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.Attributes;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import static java.util.logging.Level.*;
import static java.nio.file.FileVisitResult.*;
import static java.nio.file.StandardWatchEventKind.*;
import static java.nio.file.LinkOption.*;


public class RecursiveDirWatcher {

    private static final Logger log = Logger.getLogger(RecursiveDirWatcher.class.getName());

    private final WatchService watcher;
    private final Map<WatchKey, Path> keys;

    private final Path rootDir;

    private FSEventListener listener;

    private Filter filter;


    public RecursiveDirWatcher(Path dir, FSEventListener listener, Filter filter) throws IOException {

        this.watcher = FileSystems.getDefault().newWatchService();
        this.keys = new HashMap<>();
        this.rootDir = dir;
        this.listener = listener;
        this.filter = filter;

    }

    public void init() throws IOException {
        log.info("Scanning: "+ rootDir);
        watchFolder(rootDir);
        log.info("Done.");
    }

    /**
     * Register the given directory, and all its sub-directories, with the
     * WatchService.
     */
    private void watchFolder(Path start) throws IOException {

        SimpleFileVisitor<Path> walker = new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir) {

                if(!filter.filter(dir))
                    return SKIP_SUBTREE;

                try {
                    register(dir);
                } catch (IOException x) {
                    throw new IOError(x);
                }
                return CONTINUE;
            }

        };

        // register directory and sub-directories
        Files.walkFileTree(start, walker);
    }

    /**
     * Register the given directory with the WatchService
     */
    private void register(Path dir) throws IOException {

        WatchKey key = dir.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);

        Path prev = keys.get(key);
        if (prev == null) {
            log.info("register: "+ dir);
        } else {
            if (!dir.equals(prev)) {
                log.info("update: "+ prev +" -> "+ dir);
            }
        }

        keys.put(key, dir);
    }


    /**
     * Process all events for keys queued to the watcher
     */
    public void processEvents() throws IOException {

        while(true) {

            // wait for key to be signalled
            WatchKey key;
            try {
                key = watcher.take();
            } catch (InterruptedException ex) {
                log.log(SEVERE, "interrupted while listening", ex);
                return;
            }

            Path dir = keys.get(key);
            if (dir == null) {
                log.warning("WatchKey not recognized!!");
                continue;
            }

            for (WatchEvent<?> event : key.pollEvents()) {

                WatchEvent.Kind kind = event.kind();

                // TODO rescan
                if (kind == OVERFLOW) {
                    log.warning("internal event queue OVERFLOW");
                    continue;
                }

                // Context for directory entry event is the file name of entry
                WatchEvent<Path> ev = cast(event);
                Path child = dir.resolve(ev.context());

                // print out event
                System.out.format("%s: %s #%s\n", kind.name(), child, ev.count());

                if(kind == ENTRY_DELETE) {
                    listener.entryDeleted(child);
                }else if(kind == ENTRY_MODIFY) {
                    listener.entryModified(child);
                }else if(kind == ENTRY_CREATE) {
                    
                    listener.entryCreated(dir, child);

                    // if directory is created, and watching recursively, then
                    // register it and its sub-directories
                    try {
                        if (    Attributes.readBasicFileAttributes(child, NOFOLLOW_LINKS).isDirectory()
                            &&  filter.filter(child)
                            ) {
                                watchFolder(child);
                        }
                    } catch (IOException ex) {
                        log.log(WARNING, "unable to register dirwatcher to track file changes", ex);
                    }
                }

            }

            // reset key and remove from set if directory no longer accessible
            boolean valid = key.reset();
            if (!valid) {
                keys.remove(key);

                // all directories are inaccessible
                if (keys.isEmpty()) {
                    break;
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private <T> WatchEvent<T> cast(WatchEvent<?> event) {
        return (WatchEvent<T>)event;
    }

    public interface Filter {

        public boolean filter(Path path);

    }

}
