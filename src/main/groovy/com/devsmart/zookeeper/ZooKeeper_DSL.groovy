package com.devsmart.zookeeper

import com.devsmart.zookeeper.tasks.BuildExeTask
import com.devsmart.zookeeper.tasks.BasicTask
import com.devsmart.zookeeper.tasks.BuildLibTask

abstract class ZooKeeper_DSL extends Script {

    def exe(Closure cl) {
        BuildExeTask t = BuildExeTask.make(cl, project)
        project.addExeTask(t)
        project.addDoLast({
            project.zooKeeper.resolveTaskDependencies(t)
        })
    }

    def lib(Closure cl) {
        BuildLibTask t = BuildLibTask.make(cl, project)
        project.addLibTask(t)
        project.addDoLast({
            project.zooKeeper.resolveTaskDependencies(t)
        })
    }

    def task(Closure cl) {
        BasicTask t = BasicTask.make(cl, project)
        project.addTask(t)
        project.addDoLast({
            project.zooKeeper.resolveTaskDependencies(t)
        })
    }

    def compile(Closure cl) {
        genTemplate(cl, 'compile')
    }

    def link(Closure cl) {
        genTemplate(cl, 'link')
    }

    def staticlib(Closure cl) {
        genTemplate(cl, 'staticlib')
    }

    def sharedlib(Closure cl) {
        genTemplate(cl, 'sharedlib')
    }

    private void genTemplate(Closure cl, String stage) {
        CompileTemplate t = CompileTemplate.make(cl, project)

        for(String language : t.language) {
            TemplateKey key = new TemplateKey(t.target, language, stage)
            project.zooKeeper.templates.put(key, t)
        }

    }

}
