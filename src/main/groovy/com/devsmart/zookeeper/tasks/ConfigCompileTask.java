package com.devsmart.zookeeper.tasks;

import com.devsmart.zookeeper.StringUtils;
import com.devsmart.zookeeper.api.FileCollection;
import groovy.lang.Closure;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class ConfigCompileTask extends CompileChildProcessTask {

    private Closure< Map<String, String> > mEnvClosure;
    private FileCollection mWorkingDirPath;
    private Closure mCommandLineClosure;

    public ConfigCompileTask() {
        setDelegate(mDelegate);
    }

    void env(Closure cl) {
        mEnvClosure = cl;
    }

    void workingDir(FileCollection filePath) {
        mWorkingDirPath = filePath;
    }

    void cmd(Closure cl) {
        mCommandLineClosure = cl;
    }

    private final Delegate mDelegate = new Delegate() {

        @Override
        public String[] getCommandLine(CompileChildProcessTask task) {
            String[] retval = new String[0];
            if(mCommandLineClosure != null) {
                Object result = mCommandLineClosure.call();

                retval = StringUtils.flatten(result);

            }
            return retval;
        }

        @Override
        public File getWorkingDir(CompileChildProcessTask task) {
            File retval = null;
            if(mWorkingDirPath != null) {
                retval = mWorkingDirPath.getSingleFile();
            }
            return retval;
        }

        @Override
        public void updateEnv(CompileChildProcessTask task, Map<String, String> env) {
            if(mEnvClosure != null) {
                Map<String, String> src = mEnvClosure.call();
                StringUtils.mergeStringMaps(src, env);
            }
        }
    };

}
