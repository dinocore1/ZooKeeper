package com.devsmart.zookeeper.file;

import com.devsmart.zookeeper.api.FileVisitor;

public interface MinFileTree {

    void visit(FileVisitor fileVisitor);
}
