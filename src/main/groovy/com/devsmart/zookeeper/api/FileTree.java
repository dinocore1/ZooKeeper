package com.devsmart.zookeeper.api;

public interface FileTree extends FileCollection {

    FileTree visit(FileVisitor fileVisitor);
}
