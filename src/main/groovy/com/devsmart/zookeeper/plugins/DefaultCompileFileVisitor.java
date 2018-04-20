package com.devsmart.zookeeper.plugins;

import com.devsmart.zookeeper.DefaultProjectVisitor;
import com.devsmart.zookeeper.Platform;
import com.devsmart.zookeeper.Project;
import com.devsmart.zookeeper.api.FileCollection;
import com.devsmart.zookeeper.projectmodel.BuildableExecutable;
import com.devsmart.zookeeper.projectmodel.BuildableLibrary;
import com.devsmart.zookeeper.projectmodel.BuildableModule;
import com.devsmart.zookeeper.tasks.CompileChildProcessTask;
import com.google.common.base.Charsets;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;

import java.io.File;

public class DefaultCompileFileVisitor extends DefaultProjectVisitor {

    public static final HashFunction HASH_FUNCTION = Hashing.sha1();

    Project project;
    CompileChildProcessTask buildTask;
    Platform platform;
    String variant;
    String extra = "";

    BuildableModule module;
    CompileChildProcessTask compileTask;

    @Override
    public void visit(Project project) {
        throw new UnsupportedOperationException();

    }

    @Override
    public void visit(BuildableExecutable exe) {
        this.module = exe;

        super.visit(exe);
    }

    @Override
    public void visit(BuildableLibrary lib) {
        this.module = lib;

        super.visit(lib);
    }

    @Override
    public void visit(File srcFile) {
        FileCollection outputFile = project.file(genObjectFile(srcFile));

        compileTask = new CompileChildProcessTask();
        compileTask.setInput(project.file(srcFile));
        compileTask.setOutput(outputFile);
        project.addTask(compileTask);

        buildTask.setInput(buildTask.getInput().plus(outputFile));
    }

    File genObjectFile(File srcFile) {
        Hasher hasher = HASH_FUNCTION.newHasher();
        hasher.putString(module.getName(), Charsets.UTF_8);
        hasher.putString(module.getVersion().toString(), Charsets.UTF_8);
        hasher.putString(srcFile.getName(), Charsets.UTF_8);
        hasher.putString(extra, Charsets.UTF_8);

        String hashStr = hasher.hash().toString().substring(0, 5);

        String newName = srcFile.getName() + '_' + hashStr + ".o";

        return new File(genBuildDir(), newName);
    }

    File genBuildDir() {
        File buildDir = new File(project.getProjectDir(), "build");
        buildDir = new File(buildDir, platform.toString());
        buildDir = new File(buildDir, variant);
        return buildDir;
    }
}
