package com.devsmart.zookeeper;


import org.jgrapht.DirectedGraph;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.alg.CycleDetector;
import org.jgrapht.experimental.dag.DirectedAcyclicGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;

import java.util.HashMap;

public class DependencyGraph {

    private DirectedGraph<Action, DefaultEdge> mGraph = new DirectedAcyclicGraph<Action, DefaultEdge>(DefaultEdge.class);
    //private CycleDetector<Action, DefaultEdge> mCycleDetector = new CycleDetector<Action, DefaultEdge>(mGraph);
    private HashMap<String, Action> mActionName = new HashMap<String, Action>();
    private Action mRootAction;

    public Action getAction(String name) {
        return mActionName.get(name);
    }

    public void addAction(String name, Action action) {
        mActionName.put(name, action);
        mGraph.addVertex(action);
    }

    public void addDependency(Action thiz, Action that) {
        mGraph.addEdge(thiz, that);
        //return mCycleDetector.detectCycles();
    }
}
