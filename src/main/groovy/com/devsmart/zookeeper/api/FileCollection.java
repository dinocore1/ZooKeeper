package com.devsmart.zookeeper.api;

import java.io.File;
import java.util.Set;

public interface FileCollection extends Iterable<File> {

    /**
     * Returns a FileCollection which contains the union of
     * this collection and the given collection
     * @param collection
     * @return
     */
    FileCollection plus(FileCollection collection);

    /**
     * Returns a FileCollection which contains the difference between
     * this collection and the given collection.
     * @param collection
     * @return
     */
    FileCollection minus(FileCollection collection);

    boolean isEmpty();

    Set<File> getFiles();

    /**
     * Returns the content of this collection, asserting it contains exactly one file.
     * @return
     */
    File getSingleFile();

    /**
     * Determines whether this collection contains the given file.
     * @param file
     * @return
     */
    boolean contains(File file);

}
