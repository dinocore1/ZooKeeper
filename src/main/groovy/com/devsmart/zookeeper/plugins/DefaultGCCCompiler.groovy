package com.devsmart.zookeeper.plugins

import com.devsmart.zookeeper.DefaultProjectVisitor
import com.devsmart.zookeeper.Platform
import com.devsmart.zookeeper.Project
import com.devsmart.zookeeper.api.FileCollection
import com.devsmart.zookeeper.projectmodel.BuildableLibrary

class DefaultGCCCompiler extends DefaultProjectVisitor {

    String variant
    Platform platform
    String cmd
    List<String> cflags = []
    List<String> linkflags = []
    FileCollection includes

    protected Project project

    @Override
    void visit(Project project) {
        this.project = project
        super.visit(project)
    }


    @Override
    void visit(BuildableLibrary module) {
        GCCStaticLibVisitor staticLib = new GCCStaticLibVisitor()
        staticLib.linkCmd = cc
        staticLib.project = project
        staticLib.platform = platform
        staticLib.variant = variant
        staticLib.visit(module)

        GCCSharedLibVisitor sharedLib = new GCCSharedLibVisitor()
        sharedLib.linkCmd = cc
        sharedLib.project = project
        sharedLib.platform = platform
        sharedLib.variant = variant
        sharedLib.visit(module)

    }

}
