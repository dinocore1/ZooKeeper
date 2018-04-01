package com.devsmart.zookeeper.file;

import com.devsmart.zookeeper.DeferredUtil;

import java.io.File;
import java.nio.file.Path;

public abstract class AbstractFileResolver implements PathToFileResolver{

    public enum PathValidation {
        NONE,
        EXISTS,
        FILE,
        DIRECTORY
    }

    protected abstract File doResolve(Object path);

    public File resolve(Object path, PathValidation validation) {
        File file = doResolve(path);

        file = FileUtils.normalize(file);

        validate(file, validation);

        return file;

    }

    @Override
    public File resolve(Object path) {
        return resolve(path, PathValidation.NONE);
    }

    protected File convertObjectToFile(Object path) {
        Object object = DeferredUtil.unpack(path);
        if (object == null) {
            return null;
        }


        if(path instanceof Path) {
            return ((Path)path).toFile();
        } else if(path instanceof File) {
            return (File) path;
        } else if(path instanceof String){
            return new File((String) path);
        }

        Object converted = convert(object);
        if (converted instanceof File) {
            return (File) converted;
        }
        throw new RuntimeException(String.format("Cannot convert '%s' to a file.", converted));
    }

    private Object convert(Object input) {
        if(input instanceof Path) {
            return ((Path)input).toFile();
        } else if(input instanceof File) {
            return (File) input;
        } else if(input instanceof String){
            return new File((String) input);
        } else {
            return input;
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
