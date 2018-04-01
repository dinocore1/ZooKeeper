package com.devsmart.zookeeper.file;


import java.io.File;

public class IdentityFileResolver extends AbstractFileResolver {

    @Override
    protected File doResolve(Object path) {
        File file = convertObjectToFile(path);

        if (file == null) {
            throw new IllegalArgumentException(String.format(
                    "Cannot convert path to File. path='%s'", path));
        }

        if (!file.isAbsolute()) {
            throw new UnsupportedOperationException(String.format("Cannot convert relative path %s to an absolute file.", path));
        }
        return file;
    }
}
