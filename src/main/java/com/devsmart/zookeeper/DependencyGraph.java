package com.devsmart.zookeeper;


import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import org.jgrapht.DirectedGraph;
import org.jgrapht.experimental.dag.DirectedAcyclicGraph;
import org.jgrapht.graph.DefaultEdge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;

public class DependencyGraph {

    private static final Logger LOGGER = LoggerFactory.getLogger(DependencyGraph.class);

    private class State {
        boolean hasRun = false;
    }

    private HashMap<Action, State> mActionState = new HashMap<Action, State>();
    private DirectedGraph<Action, DefaultEdge> mGraph = new DirectedAcyclicGraph<Action, DefaultEdge>(DefaultEdge.class);
    //private CycleDetector<Action, DefaultEdge> mCycleDetector = new CycleDetector<Action, DefaultEdge>(mGraph);
    private BiMap<String, Action> mActionName = HashBiMap.create();
    private Action mRootAction;

    public Action getAction(String name) {
        return mActionName.get(name);
    }

    public void addAction(String name, Action action) {
        mActionName.put(name, action);
        mActionState.put(action, new State());
        mGraph.addVertex(action);
    }

    public void addDependency(Action thiz, Action that) {
        mGraph.addEdge(thiz, that);
        //return mCycleDetector.detectCycles();
    }

    public void runAction(Action action) {
        for(DefaultEdge e : mGraph.outgoingEdgesOf(action)) {
            Action dependency = mGraph.getEdgeTarget(e);
            State actionState = mActionState.get(dependency);
            if(!actionState.hasRun) {
                runAction(dependency);
            }
        }

        State actionState = mActionState.get(action);
        LOGGER.info("running " + mActionName.inverse().get(action));
        action.doIt();
        actionState.hasRun = true;
    }
}
