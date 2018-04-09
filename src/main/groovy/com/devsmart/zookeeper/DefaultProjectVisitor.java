package com.devsmart.zookeeper;

import com.devsmart.zookeeper.projectmodel.BuildableExecutable;
import com.devsmart.zookeeper.projectmodel.BuildableLibrary;
import com.devsmart.zookeeper.projectmodel.BuildableModule;
import com.devsmart.zookeeper.projectmodel.ProjectVisitor;

import java.io.File;

public class DefaultProjectVisitor implements ProjectVisitor {

    @Override
    public void visit(Project project) {
        for(BuildableModule module : project.buildableModules) {
            if(module instanceof BuildableExecutable) {
                visit((BuildableExecutable) module);
            }

            if(module instanceof BuildableLibrary) {
                visit((BuildableLibrary) module);
            }
        }
    }

    @Override
    public void visit(BuildableExecutable exe) {
        for(File srcFile : exe.getSrc()) {
            visit(srcFile);
        }
    }

    @Override
    public void visit(BuildableLibrary lib) {
        for(File srcFile : lib.getSrc()) {
            visit(srcFile);
        }
    }

    @Override
    public void visit(File srcFile) {
    }

}
