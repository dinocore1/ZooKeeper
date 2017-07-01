package com.devsmart.zookeeper.action;

import com.devsmart.zookeeper.Action;
import com.devsmart.zookeeper.DependencyGraph;

import java.util.concurrent.atomic.AtomicBoolean;


public class ConditionalAction implements Action {

    public final DependencyGraph dependencyGraph;
    private final AtomicBoolean condition;
    public Action conditionalAction;
    public String conditionalActionName;

    public ConditionalAction(DependencyGraph dependencyGraph, AtomicBoolean condition) {
        this.dependencyGraph = dependencyGraph;
        this.condition = condition;
    }

    @Override
    public void doIt() {
        if(condition.get()) {
            if(conditionalAction == null) {
                conditionalAction = dependencyGraph.getAction(conditionalActionName);
            }
            dependencyGraph.runAction(conditionalAction);
        }

    }
}
