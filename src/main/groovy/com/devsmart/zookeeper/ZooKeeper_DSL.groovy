package com.devsmart.zookeeper

import com.devsmart.zookeeper.tasks.BuildExeTask
import com.devsmart.zookeeper.tasks.BasicTask
import com.devsmart.zookeeper.tasks.BuildLibTask

abstract class ZooKeeper_DSL extends Script {

    def exe(Closure cl) {
        BuildExeTask t = BuildExeTask.make(cl, project)
        zooKeeper.addExeTask(t)
        zooKeeper.addDoLast({
            zooKeeper.resolveTaskDependencies(t)
        })
    }

    def lib(Closure cl) {
        BuildLibTask t = BuildLibTask.make(cl, project)
        zooKeeper.addLibTask(t)
        zooKeeper.addDoLast({
            zooKeeper.resolveTaskDependencies(t)
        })
    }

    def task(Closure cl) {
        BasicTask t = BasicTask.make(cl, project)
        project.zooKeeper.addTask(t)
        project.zooKeeper.addDoLast({
            project.zooKeeper.resolveTaskDependencies(t)
        })
    }

    def compile(Closure cl) {
        CompileTemplate t = CompileTemplate.make(cl, project)
        zooKeeper.compileTemplate = t
    }

    def link(Closure cl) {
        CompileTemplate t = CompileTemplate.make(cl, project)
        zooKeeper.linkTemplate = t
    }

}
