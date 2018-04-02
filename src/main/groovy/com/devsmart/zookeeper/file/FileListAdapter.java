package com.devsmart.zookeeper.file;

import com.google.common.base.Joiner;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

public class FileListAdapter extends AbstractFileCollection {

    private final Set<File> mFiles;

    public FileListAdapter(File... files) {
        this(Arrays.asList(files));
    }

    public FileListAdapter(Collection<File> files) {
        mFiles = new LinkedHashSet<File>(files);
    }

    @Override
    public String getDisplayName() {
        switch (mFiles.size()) {
            case 0:
                return "empty file collection";
            case 1:
                return String.format("file '%s'", mFiles.iterator().next());
            default:
                return String.format("files %s", Joiner.on(" ").join(mFiles));
        }
    }

    @Override
    public Set<File> getFiles() {
        return mFiles;
    }
}
