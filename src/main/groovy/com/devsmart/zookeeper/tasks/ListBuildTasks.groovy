package com.devsmart.zookeeper.tasks

import com.devsmart.zookeeper.ZooKeeper

class ListBuildTasks implements BuildTask {

    final ZooKeeper zooKeeper

    ListBuildTasks(ZooKeeper zk) {
        zooKeeper = zk
    }

    @Override
    boolean run() {
        System.out.println('Available tasks:')
        System.out.println(zooKeeper.dependencyGraph.taskNames.sort().join(' '))

        return true
    }
}
