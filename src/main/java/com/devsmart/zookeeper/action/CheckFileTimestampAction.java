package com.devsmart.zookeeper.action;

import com.devsmart.zookeeper.Action;
import com.devsmart.zookeeper.ZooKeeper;

import java.io.File;


public class CheckFileTimestampAction implements Action {

    private final ZooKeeper mZooKeeper;
    private final File mFileA;
    private final File mFileB;
    private final Action mActionToRun;

    public CheckFileTimestampAction(ZooKeeper zooKeeper, File a, File b, Action actionToRun) {
        mZooKeeper = zooKeeper;
        mFileA = a;
        mFileB = b;
        mActionToRun = actionToRun;
    }

    @Override
    public void doIt() throws Exception {

        long timeA = mFileA.lastModified();
        long timeB = mFileB.lastModified();

        if(timeA < timeB) {
            //todo build
        }

    }
}
