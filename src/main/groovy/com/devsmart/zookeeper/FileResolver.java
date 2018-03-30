package com.devsmart.zookeeper;

import java.io.File;
import java.nio.file.Path;

public class FileResolver {

    public enum PathValidation {
        NONE,
        EXISTS,
        FILE,
        DIRECTORY
    }

    public File resolve(Object path, PathValidation validation) {
        File file = doResolve(path);

        file = FileUtils.normalize(file);

        validate(file, validation);

        return file;

    }

    protected File doResolve(Object path) {
        Object object = DeferredUtil.unpack(path);
        if (object == null) {
            return null;
        }
        Object converted = convertObjectToFile(object);
        if (converted instanceof File) {
            return (File) converted;
        } else {
            throw new RuntimeException(String.format("Cannot convert '%s' to a file.", converted));
        }
    }

    private Object convertObjectToFile(Object path) {
        if(path == null) {
            return null;
        } else if(path instanceof Path) {
            return ((Path)path).toFile();
        } else if(path instanceof File) {
            return (File) path;
        } else {
            return null;
        }
    }

    protected void validate(File file, PathValidation validation) {
        switch (validation) {
            case NONE:
                break;
            case EXISTS:
                if (!file.exists()) {
                    throw new RuntimeException(String.format("File '%s' does not exist.", file));
                }
                break;
            case FILE:
                if (!file.exists()) {
                    throw new RuntimeException(String.format("File '%s' does not exist.", file));
                }
                if (!file.isFile()) {
                    throw new RuntimeException(String.format("File '%s' is not a file.", file));
                }
                break;
            case DIRECTORY:
                if (!file.exists()) {
                    throw new RuntimeException(String.format("Directory '%s' does not exist.", file));
                }
                if (!file.isDirectory()) {
                    throw new RuntimeException(String.format("Directory '%s' is not a directory.", file));
                }
                break;
        }
    }
}
