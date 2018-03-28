package com.devsmart.zookeeper.tasks

import com.devsmart.zookeeper.FileCollection
import com.devsmart.zookeeper.FileUtils
import com.devsmart.zookeeper.Version

class BuildExeTask extends BasicTask {

    Version version
    FileCollection sources

    def version(String versionstr) {
        setVersion(Version.fromString(versionstr))
    }

    def src(Object... paths) {
        sources = FileUtils.from(paths)
    }

    @Override
    String toString() {
        return "build: " + name
    }

    static BuildExeTask make(Closure cl) {
        BuildExeTask retval = new BuildExeTask()
        Closure code = cl.rehydrate(retval, retval, retval);
        code()
        return retval
    }
}
