package com.devsmart.zookeeper.tasks

import com.devsmart.zookeeper.api.FileCollection

class BuildLibTask extends GenericBuildTask {

    FileCollection exportHeaders

    def headers(Closure cl) {
        setExportHeaders(cl)
    }

    static BuildLibTask make(Closure cl) {
        BuildLibTask retval = new BuildLibTask()
        Closure code = cl.rehydrate(retval, retval, retval);
        code()
        return retval
    }
}
