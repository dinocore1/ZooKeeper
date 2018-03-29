package com.devsmart.zookeeper

import com.devsmart.zookeeper.tasks.BuildExeTask
import com.devsmart.zookeeper.tasks.BasicTask
import com.devsmart.zookeeper.tasks.BuildLibTask

abstract class ZooKeeper_DSL extends Script {

    def exe(Closure cl) {
        BuildExeTask t = BuildExeTask.make(cl)
        zooKeeper.addExeTask(t)
        zooKeeper.addDoLast({
            zooKeeper.resolveTaskDependencies(t)
        })
    }

    def lib(Closure cl) {
        BuildLibTask t = BuildLibTask.make(cl)
        zooKeeper.addLibTask(t)
        zooKeeper.addDoLast({
            zooKeeper.resolveTaskDependencies(t)
        })
    }

    def task(Closure cl) {
        BasicTask t = BasicTask.make(cl, zooKeeper)
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
