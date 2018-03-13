package com.devsmart.zookeeper.action;

import com.devsmart.zookeeper.Action;
import com.devsmart.zookeeper.ZooKeeper;
import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;


public class ListAllActionsAction implements Action {

    private static final Logger LOGGER = LoggerFactory.getLogger(ListAllActionsAction.class);

    private final ZooKeeper mZooKeeper;

    public ListAllActionsAction(ZooKeeper zooKeeper) {
        mZooKeeper = zooKeeper;
    }

    @Override
    public void doIt() {
        String[] actionNames = Iterables.toArray(mZooKeeper.mDependencyGraph.getTaskNames(), String.class);
        Arrays.sort(actionNames);
        LOGGER.info(Joiner.on(' ').join(actionNames));

    }
}
