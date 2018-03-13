package com.devsmart.zookeeper;


import com.devsmart.zookeeper.tasks.BuildTask;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import org.jgrapht.DirectedGraph;
import org.jgrapht.experimental.dag.DirectedAcyclicGraph;
import org.jgrapht.graph.DefaultEdge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DependencyGraph {

    private static final Logger LOGGER = LoggerFactory.getLogger(DependencyGraph.class);


    private DirectedGraph<BuildTask, DefaultEdge> mGraph = new DirectedAcyclicGraph<BuildTask, DefaultEdge>(DefaultEdge.class);
    //private CycleDetector<BuildTask, DefaultEdge> mCycleDetector = new CycleDetector<BuildTask, DefaultEdge>(mGraph);
    private BiMap<String, BuildTask> mTaskNames = HashBiMap.create();

    public BuildTask getTask(String name) {
        return mTaskNames.get(name);
    }

    public Iterable<String> getTaskNames() {
        return mTaskNames.keySet();
    }

    public void addTask(BuildTask action, String name) {
        mTaskNames.put(name, action);
        addTask(action);
    }

    public void addTask(BuildTask action) {
        mGraph.addVertex(action);
    }

    public void addDependency(BuildTask thiz, BuildTask that) {
        mGraph.addEdge(thiz, that);
        //return mCycleDetector.detectCycles();
    }

    public ExePlan createExePlan(BuildTask task) {
        ExePlan retval = new ExePlan(mGraph, task);
        return retval;
    }
}
