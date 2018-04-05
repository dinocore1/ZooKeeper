package com.devsmart.zookeeper.tasks;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashSet;

public class GenericChildProcessTask extends ChildProcessTask {

    private final LinkedHashSet<File> mIncludes = new LinkedHashSet<File>();
    private final ArrayList<String> mFlags = new ArrayList<String>();

    public void addInclude(File includeDir) {
        mIncludes.add(includeDir);
    }

    public LinkedHashSet<File> getIncludes() {
        return mIncludes;
    }

    public ArrayList<String> getFlags() {
        return mFlags;
    }

    @Override
    String[] getCommandLine() {
        return new String[0];
    }
}
