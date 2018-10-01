package com.devsmart.zookeeper;

import com.devsmart.zookeeper.tasks.BuildTask;
import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;

public class TaskRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(TaskRunner.class);
    private static final int TIMEOUT = 500;

    protected enum Color {
        WHITE, // vertex has not been seen yet
        GRAY, // vertex has been seen, but we are not yet done with all of its out-edges
        BLACK // we are done with all of its out-edges
    }

    protected DirectedGraph<BuildTask, DefaultEdge> mGraph;
    private HashMap<BuildTask, Color> mSeen = new HashMap<>();

    public TaskRunner(DependencyGraph dependencyGraph) {
        mGraph = dependencyGraph.mGraph;
    }


    private Color getColor(BuildTask task) {
        Color retval = mSeen.get(task);
        if(retval == null) {
            retval = Color.WHITE;
            mSeen.put(task, retval);
        }
        return retval;
    }

    private void setColor(BuildTask key, Color c) {
        mSeen.put(key, c);
    }

    public int build(BuildTask task) {
        int retval = 0;

        Color c = getColor(task);
        if(c == Color.WHITE) {
            setColor(task, Color.GRAY);
            for(DefaultEdge e : mGraph.outgoingEdgesOf(task)) {
                retval = build(mGraph.getEdgeTarget(e));
                if(retval != 0) {
                    return retval;
                }
            }
            retval = task.run() ? 0 : -1;

            setColor(task, Color.BLACK);
        }

        return retval;

    }
}
