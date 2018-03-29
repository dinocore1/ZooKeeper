package com.devsmart.zookeeper.tasks

class MkdirBuildTask implements BuildTask {

    public final File file

    MkdirBuildTask(File f) {
        this.file = f
    }

    @Override
    boolean run() {
        if(file.exists()) {
            return true
        } else {
            return file.mkdirs()
        }

    }
}
