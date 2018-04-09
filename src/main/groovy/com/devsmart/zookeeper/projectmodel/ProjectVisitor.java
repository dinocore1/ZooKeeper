package com.devsmart.zookeeper.projectmodel;

import com.devsmart.zookeeper.Project;

import java.io.File;

public interface ProjectVisitor {

    void visit(Project project);
    void visit(BuildableExecutable exe);
    void visit(BuildableLibrary lib);
    void visit(File srcFile);
}
