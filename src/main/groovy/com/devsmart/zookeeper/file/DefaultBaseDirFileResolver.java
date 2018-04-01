package com.devsmart.zookeeper.file;

import java.io.File;

public class DefaultBaseDirFileResolver extends AbstractBaseDirFileResolver {

    private final File mBaseDir;

    public DefaultBaseDirFileResolver(File baseDir) {
        mBaseDir = baseDir;
    }

    @Override
    public File getBaseDir() {
        return mBaseDir;
    }
}
