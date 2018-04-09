package com.devsmart.zookeeper.tasks;

import com.devsmart.zookeeper.projectmodel.BuildableExecutable;
import com.devsmart.zookeeper.projectmodel.BuildableModule;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Map;

public class DelegatingChildProcessTask extends ChildProcessTask {

    public interface Delegate {
        String[] getCommandLine(DelegatingChildProcessTask task);
        File getWorkingDir(DelegatingChildProcessTask task);
        void updateEnv(DelegatingChildProcessTask task, Map<String, String> env);
    }

    private Delegate mDelegate;
    private final LinkedHashSet<File> mIncludes = new LinkedHashSet<File>();
    private final ArrayList<String> mFlags = new ArrayList<String>();
    private BuildableModule mModule;

    public void setDelegate(Delegate d) {
        mDelegate = d;
    }

    public Delegate getDelegate() {
        return mDelegate;
    }

    public void addInclude(File includeDir) {
        mIncludes.add(includeDir);
    }

    public LinkedHashSet<File> getIncludes() {
        return mIncludes;
    }

    public ArrayList<String> getFlags() {
        return mFlags;
    }

    public void setBuildableModule(BuildableModule module) {
        mModule = module;
    }

    public BuildableModule getBuildableModule() {
        return mModule;
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
