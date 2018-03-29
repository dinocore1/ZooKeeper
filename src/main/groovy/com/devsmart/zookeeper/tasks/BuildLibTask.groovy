package com.devsmart.zookeeper.tasks

import com.devsmart.zookeeper.FileCollection
import com.devsmart.zookeeper.FileUtils
import com.devsmart.zookeeper.Version

class BuildLibTask extends BasicTask {

    Version version = Version.fromString('0.0.0')
    FileCollection sources

    def version(String versionstr) {
        setVersion(Version.fromString(versionstr))
    }

    def src(Object... paths) {
        sources = FileUtils.from(paths)
    }

    def exportHeaders(String... paths) {

    }

    @Override
    String toString() {
        return "build: " + name
    }

    static BuildLibTask make(Closure cl) {
        BuildLibTask retval = new BuildLibTask()
        Closure code = cl.rehydrate(retval, retval, retval);
        code()
        return retval
    }
}
