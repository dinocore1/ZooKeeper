package com.devsmart.zookeeper.tasks;


import java.io.File;

public class MkDirBuildTask implements BuildTask {

    File dir;

    public MkDirBuildTask(File dir) {
        this.dir = dir;
    }

    @Override
    public boolean run() {
        return dir.mkdirs();
    }
}
