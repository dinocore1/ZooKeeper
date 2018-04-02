package com.devsmart.zookeeper.tasks

import com.devsmart.zookeeper.Project

class BuildExeTask extends GenericBuildTask {

    static BuildExeTask make(Closure cl, Project project) {
        BuildExeTask retval = new BuildExeTask()
        Closure code = cl.rehydrate(retval, project, retval)
        code()
        return retval
    }
}
