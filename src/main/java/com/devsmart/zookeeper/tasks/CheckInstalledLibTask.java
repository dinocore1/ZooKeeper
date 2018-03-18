package com.devsmart.zookeeper.tasks;

import com.devsmart.zookeeper.Library;
import com.devsmart.zookeeper.Platform;
import com.devsmart.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CheckInstalledLibTask implements BuildTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(CheckInstalledLibTask.class);

    public final ZooKeeper mZookeeper;
    public Library library;
    public Platform platform;

    public CheckInstalledLibTask(ZooKeeper zooKeeper, Library depLib, Platform platform) {
        mZookeeper = zooKeeper;
        this.library = depLib;
        this.platform = platform;
    }

    @Override
    public boolean run() {




        return false;
    }
}
