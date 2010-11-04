package com.synchrony.util;

import java.nio.file.Path;


/**
 *
 * @author Michel Bien
 */
public interface FSEventListener {

    public void entryCreated(Path dir, Path child);

    public void entryModified(Path path);

    public void entryDeleted(Path path);

}
