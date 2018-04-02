package com.devsmart.zookeeper.file;

import com.devsmart.zookeeper.api.FileCollection;
import com.devsmart.zookeeper.file.CompositeFileCollection;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

public class UnionFileCollection extends CompositeFileCollection {

    private final Set<FileCollection> mSource = new LinkedHashSet<FileCollection>();

    public UnionFileCollection(FileCollection... collections) {
        mSource.addAll(Arrays.asList(collections));
    }


    public String getDisplayName() {
        return "file collection";
    }

    @Override
    public void visitContents(FileCollectionResolveContext context) {
        context.add(mSource);
    }
}
