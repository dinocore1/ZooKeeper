package com.devsmart.zookeeper.file;

import com.devsmart.zookeeper.api.FileCollection;
import com.google.common.collect.Sets;

import java.io.File;
import java.util.Collections;
import java.util.Set;

public class FileUtils {

    /**
     * Normalizes the given file, removing redundant segments like /../. If normalization
     * tries to step beyond the file system root, the root is returned.
     */
    public static File normalize(File src) {

        String path = src.getAbsolutePath();
        String normalizedPath = FilenameUtils.normalize(path);
        if (normalizedPath != null) {
            return new File(normalizedPath);
        }
        File root = src;
        File parent = root.getParentFile();
        while (parent != null) {
            root = root.getParentFile();
            parent = root.getParentFile();
        }
        return root;
    }

    public static FileCollection emptyFileCollection() {
        return new AbstractFileCollection() {
            @Override
            public String getDisplayName() {
                return "";
            }

            @Override
            public Set<File> getFiles() {
                return Collections.emptySet();
            }
        };
    }
}
