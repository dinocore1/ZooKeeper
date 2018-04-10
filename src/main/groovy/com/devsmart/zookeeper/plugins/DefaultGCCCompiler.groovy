package com.devsmart.zookeeper.plugins

import com.devsmart.zookeeper.GenericCompilerVisitor
import com.devsmart.zookeeper.Platform
import com.devsmart.zookeeper.projectmodel.BuildableExecutable
import com.devsmart.zookeeper.tasks.DelegatingChildProcessTask

class DefaultGCCCompiler extends GenericCompilerVisitor {

    String variant
    Platform platform
    String cmd
    List<String> cflags = []
    List<String> linkflags = []

    private DelegatingChildProcessTask.Delegate compileDelegate = new DelegatingChildProcessTask.Delegate() {
        
        String[] getCommandLine(DelegatingChildProcessTask task) {
            ArrayList<String> cmdline = new ArrayList<String>()
            cmdline.add(cmd)
            cmdline.addAll(cflags)
            cmdline.add('-c')
            cmdline.add('-o')
            cmdline.add(task.output.singleFile.absoluteFile.toString())
            cmdline.add(task.input.singleFile.absoluteFile.toString())
            return cmdline.toArray(new String[cmdline.size()])
        }

        File getWorkingDir(DelegatingChildProcessTask task) {
            return null
        }

        void updateEnv(DelegatingChildProcessTask task, Map<String, String> env) {

        }
    }

    private DelegatingChildProcessTask.Delegate exeLinkDelegate = new DelegatingChildProcessTask.Delegate() {
        String[] getCommandLine(DelegatingChildProcessTask task) {
            ArrayList<String> cmdline = new ArrayList<String>()
            cmdline.add(cmd)
            cmdline.addAll(linkflags)
            cmdline.add('-o')
            cmdline.add(task.output.singleFile.absoluteFile.toString())

            for(File objFile : task.input) {
                cmdline.add(objFile.absoluteFile.toString())
            }

            return cmdline.toArray(new String[cmdline.size()])
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
    void visit(BuildableExecutable module) {
        super.visit(module)
        buildTask.flags.addAll(linkflags)
        buildTask.delegate = exeLinkDelegate
    }

    @Override
    void visit(File srcFile) {
        super.visit(srcFile)
        compileTask.flags.addAll(cflags)
        compileTask.delegate = compileDelegate
    }
}
