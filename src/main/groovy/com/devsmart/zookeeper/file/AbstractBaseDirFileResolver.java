package com.devsmart.zookeeper.file;

import java.io.File;

public abstract class AbstractBaseDirFileResolver extends AbstractFileResolver {

    public abstract File getBaseDir();

    @Override
    protected File doResolve(Object path) {
        File file = convertObjectToFile(path);

        if (file == null) {
            throw new IllegalArgumentException(String.format("Cannot convert path to File. path='%s'", path));
        }

        if (!file.isAbsolute()) {
            File baseDir = getBaseDir();
            file = new File(baseDir, file.getPath());
        }

        return file;
    }
}
