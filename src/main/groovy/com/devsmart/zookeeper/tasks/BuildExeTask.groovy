package com.devsmart.zookeeper.tasks

class BuildExeTask extends GenericBuildTask {

    static BuildExeTask make(Closure cl) {
        BuildExeTask retval = new BuildExeTask()
        Closure code = cl.rehydrate(retval, retval, retval);
        code()
        return retval
    }
}
