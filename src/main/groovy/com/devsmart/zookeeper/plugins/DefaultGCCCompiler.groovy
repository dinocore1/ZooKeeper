package com.devsmart.zookeeper.plugins

import com.devsmart.zookeeper.GenericCompilerVisitor
import com.devsmart.zookeeper.Platform
import com.devsmart.zookeeper.projectmodel.BuildableExecutable
import com.devsmart.zookeeper.tasks.DelegatingChildProcessTask

class DefaultGCCCompiler extends GenericCompilerVisitor {

    String variant
    Platform platform
    List<String> cflags = []
    List<String> linkflags = []

    private DelegatingChildProcessTask.Delegate compileDelegate = new DelegatingChildProcessTask() {
        String[] getCommandLine(DelegatingChildProcessTask task) {


        }

        File getWorkingDir(DelegatingChildProcessTask task) {
            return null
        }

        void updateEnv(DelegatingChildProcessTask task, Map<String, String> env) {

        }
    }

    File genExeOutputFile(BuildableExecutable exe) {
        return new File(genBuildDir(), exe.name)
    }

    @Override
    void visit(File srcFile) {
        super.visit(srcFile)
        compileTask.flags.addAll(cflags)
        compileTask.delegate = compileDelegate
    }
}
