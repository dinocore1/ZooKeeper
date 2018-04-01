package com.devsmart.zookeeper.file;

import com.devsmart.zookeeper.api.FileCollection;
import com.devsmart.zookeeper.file.AbstractFileCollection;

import java.io.File;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

abstract public class CompositeFileCollection extends AbstractFileCollection {

    abstract Set<FileCollection> getSourceCollections();

    @Override
    public Set<File> getFiles() {
        // Gather each of the backing Sets first, so we can set the initial capacity of the LinkedHashSet
        List<Set<File>> fileSets = new LinkedList<Set<File>>();
        int fileCount = 0;
        for (FileCollection collection : getSourceCollections()) {
            Set<File> files = collection.getFiles();
            fileCount += files.size();
            fileSets.add(files);
        }
        Set<File> allFiles = new LinkedHashSet<File>(fileCount);
        for (Set<File> fileSet : fileSets) {
            allFiles.addAll(fileSet);
        }
        return allFiles;
    }
}
