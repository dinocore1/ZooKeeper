package com.devsmart.zookeeper

import com.devsmart.zookeeper.tasks.BuildExeTask
import com.devsmart.zookeeper.tasks.BasicTask

abstract class ZooKeeper_DSL extends Script {

    def exe(Closure cl) {
        BuildExeTask t = BuildExeTask.make(cl)
        zooKeeper.addExeTask(t)
        zooKeeper.addDoLast({
            zooKeeper.resolveTaskDependencies(t)
        })
    }

    def lib(Closure cl) {

    }

    def task(Closure cl) {
        BasicTask t = BasicTask.make(cl)
        zooKeeper.addTask(t)
        zooKeeper.addDoLast({
            zooKeeper.resolveTaskDependencies(t)
        })
    }

    def compile(Closure cl) {
        CompileTemplate t = CompileTemplate.make(cl)
        zooKeeper.compileTemplate = t
    }

    def link(Closure cl) {
        CompileTemplate t = CompileTemplate.make(cl)
        zooKeeper.linkTemplate = t
    }

}
