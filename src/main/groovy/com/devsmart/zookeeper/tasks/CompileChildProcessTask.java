package com.devsmart.zookeeper.tasks;

import com.devsmart.zookeeper.plugins.CompileContext;
import com.devsmart.zookeeper.plugins.CompileProcessModifier;
import com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CompileChildProcessTask extends ChildProcessTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(CompileChildProcessTask.class);

    public interface Delegate {
        String[] getCommandLine(CompileChildProcessTask task);
        File getWorkingDir(CompileChildProcessTask task);
        void updateEnv(CompileChildProcessTask task, Map<String, String> env);
    }

    private Delegate mDelegate;
    private CompileContext mCompileContext = new CompileContext();
    private List<CompileProcessModifier> mModifiers = new ArrayList<>();

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
        Preconditions.checkNotNull(modifier);
        mModifiers.add(modifier);
    }

    private long latestInputFileTime() {
        long retval = 0;
        for(File f : getInput()) {
            if(f.exists()) {
                retval = Math.max(retval, f.lastModified());
            }
        }
        return retval;
    }

    @Override
    public boolean run() {
        File outputFile = getOutput().getSingleFile();

        if(!outputFile.exists()
                || latestInputFileTime() > outputFile.lastModified() ) {
            return super.run();
        } else {
            LOGGER.info("{} is up to date", outputFile.getName());
            return true;
        }

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

    public void doModify() {
        for(CompileProcessModifier modifier : mModifiers) {
            modifier.apply(this);
        }

    }


}
