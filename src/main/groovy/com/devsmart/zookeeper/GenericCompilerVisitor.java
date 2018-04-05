package com.devsmart.zookeeper;

import com.devsmart.zookeeper.api.FileCollection;
import com.devsmart.zookeeper.projectmodel.BuildableExecutable;
import com.devsmart.zookeeper.projectmodel.BuildableModule;
import com.devsmart.zookeeper.tasks.BasicTask;
import com.devsmart.zookeeper.tasks.ChildProcessTask;
import com.devsmart.zookeeper.tasks.GenericChildProcessTask;
import org.apache.commons.lang.StringUtils;

import java.io.File;

public abstract class GenericCompilerVisitor extends DefaultProjectVisitor {

    Project project;
    BuildableModule module;
    GenericChildProcessTask buildTask;
    GenericChildProcessTask compileTask;

    abstract Platform getPlatform();
    abstract String getVariant();
    abstract File genExeOutputFile(BuildableExecutable exe);

    @Override
    public void visit(Project project) {
        this.project = project;
        super.visit(project);
    }

    @Override
    public void visit(BuildableExecutable module) {
        this.module = module;

        buildTask = new GenericChildProcessTask();
        buildTask.setName(genExeTaskName(module));
        buildTask.setOutput(genExeOutputs(module));


        super.visit(module);

    }

    @Override
    public void visit(File srcFile) {
        compileTask = new GenericChildProcessTask();
        compileTask.setInput(project.file(srcFile));

    }

    String genExeTaskName(BuildableExecutable exe) {
        return "link"
                + StringUtils.capitalize(exe.getName())
                + StringUtils.capitalize(getPlatform().toString())
                + StringUtils.capitalize(getVariant());
    }

    File genBuildDir() {
        File buildDir = new File(project.getProjectDir(), "build");
        buildDir = new File(buildDir, getPlatform().toString());
        buildDir = new File(buildDir, getVariant());
        return buildDir;
    }

    FileCollection genExeOutputs(BuildableExecutable exe) {
        return project.file(genExeOutputFile(exe));
    }




}
