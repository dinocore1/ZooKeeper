package com.devsmart.zookeeper.file;

import com.devsmart.zookeeper.api.FileDetails;
import com.devsmart.zookeeper.api.FileTree;
import com.devsmart.zookeeper.api.FileVisitor;

import java.io.File;
import java.util.LinkedHashSet;
import java.util.Set;

public abstract class AbstractFileTree extends AbstractFileCollection implements FileTree {

    public Set<File> getFiles() {
        final Set<File> files = new LinkedHashSet<File>();
        visit(new FileVisitor() {

            @Override
            public void visit(FileDetails fileDetails) {
                if(!fileDetails.isDirectory()) {
                    files.add(fileDetails.getFile());
                }
            }

        });
        return files;
    }
}
