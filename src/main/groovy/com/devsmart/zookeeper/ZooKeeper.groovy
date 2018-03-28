package com.devsmart.zookeeper

import com.devsmart.zookeeper.tasks.BuildExeTask
import com.devsmart.zookeeper.tasks.BuildTask
import com.devsmart.zookeeper.tasks.BasicTask
import org.codehaus.groovy.control.CompilerConfiguration
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class ZooKeeper {

    private static final Logger LOGGER = LoggerFactory.getLogger(ZooKeeper.class)

    public final DependencyGraph dependencyGraph = new DependencyGraph()
    private final List<Runnable> mDoLast = []


    void resolveTaskDependencies(BasicTask t) {
        for(String taskName : t.dependencies) {
            BuildTask childTask = dependencyGraph.getTask(taskName)
            if(childTask == null) {
                LOGGER.warn("could not resolve task: {}", taskName)
            } else {
                dependencyGraph.addDependency(t, childTask)
            }
        }
    }

    void addTask(BasicTask t) {
        String taskName = t.name
        if(taskName != null) {
            dependencyGraph.addTask(t, taskName)
        } else {
            dependencyGraph.addTask(t)
        }
    }

    void addExeTask(BuildExeTask t) {
        addTask(t)


    }

    void addDoLast(Runnable r) {
        mDoLast.add(r)
    }

    void runDoLast() {
        for(Runnable r : mDoLast) {
            r.run()
        }
    }

    static void main(String[] args) {

        ZooKeeper zooKeeper = new ZooKeeper()

        File buildFile = new File("build.zoo")
        if(buildFile.exists()) {
            CompilerConfiguration cc = new CompilerConfiguration()
            cc.scriptBaseClass = 'com.devsmart.zookeeper.ZooKeeper_DSL'
            Binding binding = new Binding()
            binding.setProperty("zooKeeper", zooKeeper)
            GroovyShell shell = new GroovyShell(binding, cc)

            Script script = shell.parse(buildFile)
            script.run()

            zooKeeper.runDoLast()

        } else {
            LOGGER.error("no build.zoo file found")
            System.exit(-1)
        }

    }

}
