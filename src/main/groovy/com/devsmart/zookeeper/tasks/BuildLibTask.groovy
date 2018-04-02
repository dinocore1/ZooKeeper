package com.devsmart.zookeeper.tasks

import com.devsmart.zookeeper.Project

class BuildLibTask extends GenericBuildTask {

    CopyTask exportHeaders

    def exportHeaders(Closure cl) {

    }

    static BuildLibTask make(Closure cl, Project project) {
        BuildLibTask retval = new BuildLibTask()
        Closure code = cl.rehydrate(retval, project, retval)
        code()
        return retval
    }
}
