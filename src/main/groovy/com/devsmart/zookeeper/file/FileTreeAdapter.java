package com.devsmart.zookeeper.file;

import com.devsmart.zookeeper.api.FileTree;
import com.devsmart.zookeeper.api.FileVisitor;

public class FileTreeAdapter extends AbstractFileTree {

    private final MinFileTree mTree;

    public FileTreeAdapter(MinFileTree fileTree) {
        mTree = fileTree;
    }

    public MinFileTree getTree() {
        return mTree;
    }

    @Override
    public FileTree visit(FileVisitor fileVisitor) {
        mTree.visit(fileVisitor);
        return this;
    }

    @Override
    public String getDisplayName() {
        return null;
    }
}
