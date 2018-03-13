package com.devsmart.zookeeper;


import com.devsmart.zookeeper.tasks.BuildTask;
import org.jetbrains.annotations.NotNull;
import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultEdge;

import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.*;

public class ExePlan {
    private final DirectedGraph<BuildTask, DefaultEdge> mDependencyGraph;
    private final BuildTask mTarget;

    public ExePlan(DirectedGraph<BuildTask, DefaultEdge> dependencyGraph, BuildTask target) {
        mDependencyGraph = dependencyGraph;
        mTarget = target;
    }

    public boolean run(int maxThreads) throws ExecutionException, InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(maxThreads);

        Future<Boolean> future = run(executorService, mTarget);
        return future.get();
    }

    Future<Boolean> run(ExecutorService executorService, final BuildTask target) {

        Set<DefaultEdge> childTasks = mDependencyGraph.outgoingEdgesOf(mTarget);

        if(childTasks.isEmpty()) {
            return executorService.submit(new Callable<Boolean>(){
                @Override
                public Boolean call() throws Exception {
                    return target.run();
                }
            });
        } else {
            return new AllDoneFuture(executorService, childTasks);
        }
    }

    private class AllDoneFuture implements Future<Boolean> {

        private final ArrayList<Future<Boolean>> mChildTasks;
        private boolean mIsDone = false;
        private boolean mIsCanceled = false;

        AllDoneFuture(ExecutorService executorService, Set<DefaultEdge> childTasks) {
            mChildTasks = new ArrayList<>(childTasks.size());
            for(DefaultEdge edge : childTasks) {
                BuildTask childTask = mDependencyGraph.getEdgeTarget(edge);
                mChildTasks.add(ExePlan.this.run(executorService, childTask));
            }
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            mIsCanceled = true;
            for(Future<Boolean> childTask : mChildTasks) {
                boolean couldChildBeCanceled = childTask.cancel(mayInterruptIfRunning);
                if(!couldChildBeCanceled) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public boolean isCancelled() {
            return mIsCanceled;
        }

        @Override
        public boolean isDone() {
            return mIsDone;
        }

        @Override
        public Boolean get() throws InterruptedException, ExecutionException {
            try {
                for (Future<Boolean> childTask : mChildTasks) {
                    boolean childSuccess = childTask.get();
                    if (!childSuccess) {
                        return false;
                    }
                }
                return true;
            } finally {
                mIsDone = true;
            }
        }

        @Override
        public Boolean get(long timeout, @NotNull TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
            try {
                for (Future<Boolean> childTask : mChildTasks) {
                    boolean childSuccess = childTask.get(timeout, unit);
                    if (!childSuccess) {
                        return false;
                    }
                }
                return true;
            } finally {
                mIsDone = true;
            }
        }
    }



}
