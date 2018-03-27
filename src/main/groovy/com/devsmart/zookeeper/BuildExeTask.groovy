package com.devsmart.zookeeper

class BuildExeTask extends Task {

    Version version

    def version(String versionstr) {
        setVersion(Version.fromString(versionstr))
    }

    def src(String... names) {

    }

    static BuildExeTask make(Closure cl) {
        BuildExeTask retval = new BuildExeTask()
        Closure code = cl.rehydrate(retval, retval, retval);
        code()
        return retval
    }
}
