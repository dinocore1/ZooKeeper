package com.devsmart.zookeeper.tasks;

import com.devsmart.zookeeper.plugins.CompileContext;
import com.devsmart.zookeeper.plugins.CompileProcessModifier;

import java.io.File;
import java.util.Map;

public class CompileChildProcessTask extends ChildProcessTask {

    public interface Delegate {
        String[] getCommandLine(CompileChildProcessTask task);
        File getWorkingDir(CompileChildProcessTask task);
        void updateEnv(CompileChildProcessTask task, Map<String, String> env);
    }

    private Delegate mDelegate;
    private CompileContext mCompileContext = new CompileContext();

    public void setDelegate(Delegate d) {
        mDelegate = d;
    }

    public Delegate getDelegate() {
        return mDelegate;
    }

    public CompileContext getCompileContext() {
        return mCompileContext;
    }

    public void addModifier(CompileProcessModifier modifier) {

    }


    @Override
    public File getWorkingDir() {
        return mDelegate.getWorkingDir(this);
    }

    @Override
    public void updateEnv(Map<String, String> env) {
        mDelegate.updateEnv(this, env);
    }

    @Override
    String[] getCommandLine() {
        return mDelegate.getCommandLine(this);
    }


}
