package com.devsmart.zookeeper

import com.devsmart.zookeeper.tasks.BuildExeTask
import com.devsmart.zookeeper.tasks.BuildTask
import com.devsmart.zookeeper.tasks.BasicTask
import org.codehaus.groovy.control.CompilerConfiguration

class ZooKeeper {

    public final DependencyGraph dependencyGraph = new DependencyGraph()


    void resolveTaskDependencies(BasicTask t) {
        for(String taskName : t.dependcies) {
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
            zooKeeper.dependencyGraph.addTask(t, taskName)
        } else {
            zooKeeper.dependencyGraph.addTask(t)
        }
    }

    void addExeTask(BuildExeTask task) {


    }

    private void resolveDependicies(BasicTask t) {
        for(String taskName : t.dependcies) {
            zooKeeper.dependencyGraph.getTask(taskName)
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



        }

    }

}
