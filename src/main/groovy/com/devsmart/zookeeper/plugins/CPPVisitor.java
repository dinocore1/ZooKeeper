package com.devsmart.zookeeper.plugins;

import com.devsmart.zookeeper.tasks.CompileChildProcessTask;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;

public class CPPVisitor extends DefaultCompileFileVisitor {

    String compilerCmd;

    @Override
    public void visit(File srcFile) {
        if(srcFile.getName().endsWith(".cpp") || srcFile.getName().endsWith(".cc")) {
            super.visit(srcFile);
            compileTask.setDelegate(compileDelegate);
        }
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
