package com.devsmart.zookeeper

import com.devsmart.zookeeper.projectmodel.BuildableExecutable
import com.devsmart.zookeeper.projectmodel.BuildableLibrary
import com.devsmart.zookeeper.projectmodel.PrecompiledLibrary
import com.devsmart.zookeeper.tasks.BuildExeTask
import com.devsmart.zookeeper.tasks.BasicTask
import com.devsmart.zookeeper.tasks.BuildLibTask

abstract class ZooKeeper_DSL extends Script {

    def exe(Closure cl) {
        BuildableExecutable exe = new BuildableExecutable()
        Closure code = cl.rehydrate(exe, project, exe)
        code()

        project.modules.add(exe)
    }

    def lib(Closure cl) {
        BuildableLibrary lib = new BuildableLibrary()
        Closure code = cl.rehydrate(lib, project, lib)
        code()

        project.modules.add(lib)
    }

    def task(Closure cl) {

        /*

        BasicTask t = BasicTask.make(cl, project)
        project.addTask(t)
        project.addDoLast({
            project.zooKeeper.resolveTaskDependencies(t)
        })

        */
    }

    def precompiledLib(Closure cl) {
        PrecompiledLibrary lib = new PrecompiledLibrary()
        Closure code = cl.rehydrate(lib, project, lib)
        code()

        project.modules.add(lib)

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
