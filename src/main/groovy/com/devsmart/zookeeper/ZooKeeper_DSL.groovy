package com.devsmart.zookeeper

import com.devsmart.zookeeper.tasks.BuildExeTask
import com.devsmart.zookeeper.tasks.BasicTask

abstract class ZooKeeper_DSL extends Script {

    private List<Runnable> mDoLast = []

    def exe(Closure cl) {
        BuildExeTask t = BuildExeTask.make(cl)
        zooKeeper.addExeTask(t)
        mDoLast.add({
            zooKeeper.resolveTaskDependencies(t)
        })
    }

    def task(Closure cl) {
        BasicTask t = BasicTask.make(cl)
        zooKeeper.addTask(t)
        mDoLast.add({
            zooKeeper.resolveTaskDependencies(t)
        })
    }

    def compile(Closure cl) {
        CompileTemplate t = CompileTemplate.make(cl)
    }

}
