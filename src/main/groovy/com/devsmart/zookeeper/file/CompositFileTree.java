package com.devsmart.zookeeper.file;

import com.devsmart.zookeeper.api.FileTree;
import com.devsmart.zookeeper.api.FileVisitor;

import java.util.Collection;

public abstract class CompositFileTree extends CompositeFileCollection implements FileTree {

    protected Collection<? extends FileTree> getSourceCollections() {
        return (Collection<? extends FileTree>) super.getSourceCollections();
    }

    @Override
    public FileTree visit(FileVisitor fileVisitor) {
        for (FileTree tree : getSourceCollections()) {
            tree.visit(fileVisitor);
        }
        return this;
    }
}
