package com.devsmart.zookeeper

abstract class ZooKeeper_DSL extends Script {

    ZooKeeper zooKeeper
    private List<Task> mResolveQueue = []

    private void addTask(Task t) {
        String taskName = t.name
        if(taskName != null) {
            zooKeeper.dependencyGraph.addTask(t, taskName)
        } else {
            zooKeeper.dependencyGraph.addTask(t)
        }
        mResolveQueue.add(t)
    }

    private void resolveDependicies(Task t) {
        for(String taskName : t.dependcies) {
            zooKeeper.dependencyGraph.getTask(taskName)
        }
    }

    def exe(Closure cl) {
        BuildExeTask t = BuildExeTask.make(cl)
        addTask(t)
    }

    def task(Closure cl) {
        Task t = Task.make(cl)
        addTask(t)
    }

}
