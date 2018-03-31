package com.devsmart.zookeeper;

import com.devsmart.zookeeper.api.FileCollection;

import java.io.File;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

abstract class AbstractFileCollection implements FileCollection {


    public String getDisplayName() {
        return "";
    }

    @Override
    public FileCollection plus(FileCollection collection) {
        return new UnionFileCollection(this, collection);
    }

    @Override
    public FileCollection minus(FileCollection collection) {
        return new AbstractFileCollection() {

            @Override
            public Set<File> getFiles() {
                Set<File> files = new LinkedHashSet<File>(AbstractFileCollection.this.getFiles());
                files.removeAll(collection.getFiles());
                return files;
            }
        };
    }

    @Override
    public String toString() {
        return getDisplayName();
    }

    @Override
    public boolean isEmpty() {
        return getFiles().isEmpty();
    }

    @Override
    public File getSingleFile() {
        Collection<File> files = getFiles();
        if (files.isEmpty()) {
            throw new IllegalStateException(String.format("Expected %s to contain exactly one file, however, it contains no files.", getDisplayName()));
        }
        if (files.size() != 1) {
            throw new IllegalStateException(String.format("Expected %s to contain exactly one file, however, it contains %d files.", getDisplayName(), files.size()));
        }
        return files.iterator().next();
    }

    @Override
    public boolean contains(File file) {
        return getFiles().contains(file);
    }

    @Override
    public Iterator<File> iterator() {
        return getFiles().iterator();
    }
}
