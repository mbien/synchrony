package com.synchrony.util;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
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

    public static void deleteDir(Path dir) throws IOException {
        deleteDir(dir, true);
    }
    
    public static void deleteFilesInDir(Path dir) throws IOException {
        deleteDir(dir, false);
    }

    private static void deleteDir(final Path dir, final boolean deleteDir) throws IOException {

        Files.walkFileTree(dir, new SimpleFileVisitor<Path>() {

            @Override
            public FileVisitResult postVisitDirectory(Path path, IOException exc) {
                if(!deleteDir && !dir.equals(path)) {
                    try {
                        path.delete();
                    } catch (IOException ex) {
                        Logger.getLogger(IOUtils.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                try {
                    file.delete();
                } catch (IOException ex) {
                    Logger.getLogger(IOUtils.class.getName()).log(Level.SEVERE, null, ex);
                }
                return FileVisitResult.CONTINUE;
            }
        });

    }
    
    /**
     * @see java.​nio.​file.​attribute.​FileTime#compareTo(FileTime other)
     */
    public static int compareCreationTimes(Path file1, Path file2) throws IOException {

        FileTime time1 = readBasicAtributes(file1).creationTime();
        FileTime time2 = readBasicAtributes(file2).creationTime();

        return time1.compareTo(time2);
    }

    /**
     * @see java.​nio.​file.​attribute.​FileTime#compareTo(FileTime other)
     */
    public static int compareModificationTimes(Path file1, Path file2) throws IOException {

        FileTime time1 = readBasicAtributes(file1).lastModifiedTime();
        FileTime time2 = readBasicAtributes(file2).lastModifiedTime();

        return time1.compareTo(time2);
    }

    /**
     * @see java.​nio.​file.​attribute.​FileTime#compareTo(FileTime other)
     */
    public static int compareAccessTimes(Path file1, Path file2) throws IOException {

        FileTime time1 = readBasicAtributes(file1).lastAccessTime();
        FileTime time2 = readBasicAtributes(file2).lastAccessTime();

        return time1.compareTo(time2);
    }

    public static BasicFileAttributes readBasicAtributes(Path entry) throws IOException {
        return Attributes.readBasicFileAttributes(entry, LinkOption.NOFOLLOW_LINKS);
    }

    public static boolean isDirectory(Path entry) throws IOException {
        return readBasicAtributes(entry).isDirectory();
    }
    
    public static void transfer(ReadableByteChannel src, WritableByteChannel dest, ByteBuffer buffer) throws IOException {
        buffer.clear();
        while(src.read(buffer) != -1) {
            buffer.flip();
            dest.write(buffer);
            buffer.compact();
        }
        
        // EOF will leave buffer in fill state
        buffer.flip();
        while (buffer.hasRemaining()) {
          dest.write(buffer);
        }
    }
    
    public static ByteBuffer newDirectByteBuffer(int length) {
        return ByteBuffer.allocateDirect(length).order(ByteOrder.nativeOrder());
    }

}
