package com.devsmart.zookeeper;

import com.devsmart.zookeeper.api.FileCollection;
import com.devsmart.zookeeper.file.UnionFileCollection;
import com.devsmart.zookeeper.projectmodel.BuildableExecutable;
import com.devsmart.zookeeper.projectmodel.BuildableModule;
import com.devsmart.zookeeper.tasks.DelegatingChildProcessTask;
import com.google.common.base.Charsets;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;
import org.apache.commons.lang.StringUtils;

import java.io.File;

public abstract class GenericCompilerVisitor extends DefaultProjectVisitor {

    public static final HashFunction HASH_FUNCTION = Hashing.sha1();

    public Project project;
    public BuildableModule module;
    public DelegatingChildProcessTask buildTask;
    public DelegatingChildProcessTask compileTask;

    public abstract Platform getPlatform();
    public abstract String getVariant();
    public abstract File genExeOutputFile(BuildableExecutable exe);

    @Override
    public void visit(Project project) {
        this.project = project;
        super.visit(project);
    }

    @Override
    public void visit(BuildableExecutable module) {
        this.module = module;

        buildTask = new DelegatingChildProcessTask();
        buildTask.setBuildableModule(module);
        buildTask.setName(genExeTaskName(module));
        buildTask.setOutput(genExeOutputs(module));

        super.visit(module);

    }

    @Override
    public void visit(File srcFile) {
        FileCollection outputFile = project.file(genObjectFile(srcFile));

        compileTask = new DelegatingChildProcessTask();
        compileTask.setInput(project.file(srcFile));
        compileTask.setOutput(outputFile);


        buildTask.setInput(new UnionFileCollection(outputFile, buildTask.getInput()));
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


    File genObjectFile(File srcFile) {
        Hasher hasher = HASH_FUNCTION.newHasher();
        hasher.putString(module.getName(), Charsets.UTF_8);
        hasher.putString(module.getVersion().toString(), Charsets.UTF_8);
        hasher.putString(srcFile.getName(), Charsets.UTF_8);

        String hashStr = hasher.hash().toString().substring(0, 5);

        String newName = srcFile.getName() + '_' + hashStr + ".o";

        return new File(genBuildDir(), newName);

    }




}
