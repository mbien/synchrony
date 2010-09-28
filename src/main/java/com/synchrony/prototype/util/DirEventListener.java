package com.synchrony.prototype.util;

import java.nio.file.Path;



/**
 *
 * @author Michel Bien
 */
public interface DirEventListener {

    public void entryCreated(Path dir, Path child);

    public void entryModified(Path path);

    public void entryDeleted(Path path);


}
