package com.devsmart.zookeeper

abstract class ZooKeeper_DSL extends Script {

    def exe(Closure cl) {
        BuildExeTask t = BuildExeTask.make(cl)
    }

    def task(Closure cl) {
        Task t = Task.make(cl)
    }

}
