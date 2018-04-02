package com.devsmart.zookeeper.file;

import com.devsmart.zookeeper.api.FileCollection;
import com.devsmart.zookeeper.file.AbstractFileCollection;

import java.io.File;
import java.util.*;

abstract public class CompositeFileCollection extends AbstractFileCollection {

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

    public abstract void visitContents(FileCollectionResolveContext context);

    protected Collection<? extends FileCollection> getSourceCollections() {
        DefaultFileCollectionResolveContext context = new DefaultFileCollectionResolveContext(new IdentityFileResolver());
        visitContents(context);
        return context.resolveAsFileCollections();
    }


}
