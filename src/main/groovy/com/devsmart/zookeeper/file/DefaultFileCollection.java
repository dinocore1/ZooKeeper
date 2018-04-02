package com.devsmart.zookeeper.file;

import com.devsmart.zookeeper.api.FileCollection;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

public class DefaultFileCollection extends CompositeFileCollection {

    private final String mDisplayName;
    private final PathToFileResolver mFileResolver;
    private final LinkedHashSet<Object> mFiles = new LinkedHashSet<Object>();

    public DefaultFileCollection(String displayName, PathToFileResolver fileResolver, Object... paths) {
        this(displayName, fileResolver, Arrays.asList(paths));
    }

    public DefaultFileCollection(String displayName, PathToFileResolver fileResolver, Collection<?> paths) {
        mDisplayName = displayName;
        mFileResolver = fileResolver;
        if(paths != null) {
            mFiles.addAll(paths);
        }
    }

    @Override
    public void visitContents(FileCollectionResolveContext context) {
        FileCollectionResolveContext nested = context.push(mFileResolver);
        nested.add(mFiles);
    }

    @Override
    public String getDisplayName() {
        return mDisplayName;
    }
}
