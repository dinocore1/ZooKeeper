package com.devsmart.zookeeper.plugins;

import com.devsmart.zookeeper.projectmodel.BuildableLibrary;
import com.devsmart.zookeeper.tasks.CompileChildProcessTask;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Map;

public class GnuCompilerVisitor extends BasicCompilerFileVisitor {


    String compilerCmd;
    CompileProcessModifier compileSettings;
    FileFilter fileFilter;

    protected BuildableLibrary mLibrary;
    protected CompileProcessModifier mDependenciesModifier;


    @Override
    public void visit(BuildableLibrary lib) {
        this.mLibrary = lib;
        createDependencyModifier();
        super.visit(lib);
    }

    @Override
    public void visit(File srcFile) {

        if(fileFilter.accept(srcFile)) {
            super.visit(srcFile);
            compileTask.addModifier(compileSettings);
            compileTask.addModifier(mDependenciesModifier);
            compileTask.setDelegate(compileDelegate);
        }
    }

    private void createDependencyModifier() {
        mDependenciesModifier = new CompileProcessModifier() {
            @Override
            public void apply(CompileChildProcessTask ctx) {

            }
        };
    }

    private CompileChildProcessTask.Delegate compileDelegate = new CompileChildProcessTask.Delegate() {

        @Override
        public String[] getCommandLine(CompileChildProcessTask task) {
            CompileContext compileContext = task.getCompileContext();
            ArrayList<String> cmdline = new ArrayList<>();
            cmdline.add(compilerCmd);
            cmdline.addAll(compileContext.flags);
            cmdline.add("-c");

            for(File includeDir : compileContext.includes) {
                cmdline.add("-I" + includeDir.getAbsoluteFile().toString());
            }

            cmdline.add("-o");
            cmdline.add(task.getOutput().getSingleFile().getAbsoluteFile().toString());
            cmdline.add(task.getInput().getSingleFile().getAbsoluteFile().toString());
            return cmdline.toArray(new String[cmdline.size()]);
        }

        @Override
        public File getWorkingDir(CompileChildProcessTask task) {
            return null;
        }

        @Override
        public void updateEnv(CompileChildProcessTask task, Map<String, String> env) {

        }
    };
}
