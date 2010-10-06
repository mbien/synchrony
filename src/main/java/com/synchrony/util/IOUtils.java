package com.synchrony.util;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.Attributes;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Michael Bien
 */
public class IOUtils {

    public static void deleteDir(Path dir) {
        deleteDir(dir, true);
    }
    
    public static void deleteFilesInDir(Path dir) {
        deleteDir(dir, false);
    }

    private static void deleteDir(final Path dir, final boolean deleteDir) {

        Files.walkFileTree(dir, new SimpleFileVisitor<Path>() {

            @Override
            public FileVisitResult postVisitDirectory(Path path, IOException exc) {
                if(!deleteDir && !dir.equals(path)) {
                    try {
                        path.delete();
                    } catch (IOException ex) {
                        Logger.getLogger(RecursiveDirWatcher.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                try {
                    file.delete();
                } catch (IOException ex) {
                    Logger.getLogger(RecursiveDirWatcher.class.getName()).log(Level.SEVERE, null, ex);
                }
                return FileVisitResult.CONTINUE;
            }
        });

    }

    /**
     * @see java.​nio.​file.​attribute.​FileTime#compareTo(FileTime other)
     */
    public static int compateCreationTimes(Path file1, Path file2) throws IOException {

        FileTime time1 = Attributes.readBasicFileAttributes(file1, LinkOption.NOFOLLOW_LINKS).creationTime();
        FileTime time2 = Attributes.readBasicFileAttributes(file2, LinkOption.NOFOLLOW_LINKS).creationTime();

        return time1.compareTo(time2);
    }

    /**
     * @see java.​nio.​file.​attribute.​FileTime#compareTo(FileTime other)
     */
    public static int compateModificationTimes(Path file1, Path file2) throws IOException {

        FileTime time1 = Attributes.readBasicFileAttributes(file1, LinkOption.NOFOLLOW_LINKS).lastModifiedTime();
        FileTime time2 = Attributes.readBasicFileAttributes(file2, LinkOption.NOFOLLOW_LINKS).lastModifiedTime();

        return time1.compareTo(time2);
    }

    /**
     * @see java.​nio.​file.​attribute.​FileTime#compareTo(FileTime other)
     */
    public static int compateAccessTimes(Path file1, Path file2) throws IOException {

        FileTime time1 = Attributes.readBasicFileAttributes(file1, LinkOption.NOFOLLOW_LINKS).lastAccessTime();
        FileTime time2 = Attributes.readBasicFileAttributes(file2, LinkOption.NOFOLLOW_LINKS).lastAccessTime();

        return time1.compareTo(time2);
    }


}
