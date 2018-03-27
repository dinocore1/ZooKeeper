package com.devsmart.zookeeper;


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

        Future<Boolean> future = createFuture(executorService, mTarget);
        return future.get();
    }

    Future<Boolean> createFuture(ExecutorService executorService, final BuildTask target) {
        Set<DefaultEdge> outEdges = mDependencyGraph.outgoingEdgesOf(target);
        if(outEdges.isEmpty()) {
            return executorService.submit(new Callable<Boolean>() {
                @Override
                public Boolean call() throws Exception {
                    return target.run();
                }
            });
        } else {
            ArrayList<Future<Boolean>> children = new ArrayList<>(outEdges.size());
            for(DefaultEdge edge : outEdges) {
                BuildTask childTask = mDependencyGraph.getEdgeTarget(edge);
                children.add(createFuture(executorService, childTask));
            }
            return new AllDoneFuture(executorService, target, children);
        }
    }

    private class AllDoneFuture implements Future<Boolean> {

        private final ExecutorService mExecutorService;
        private final BuildTask mLocalTarget;
        private final ArrayList<Future<Boolean>> mChildren;

        private boolean mIsDone = false;
        private boolean mIsCanceled = false;

        AllDoneFuture(ExecutorService executorService, BuildTask target, ArrayList<Future<Boolean>> children) {
            mExecutorService = executorService;
            mLocalTarget = target;
            mChildren = children;
        }

        @Override
        public synchronized boolean cancel(boolean mayInterruptIfRunning) {
            mIsCanceled = true;
            return true;
        }

        @Override
        public synchronized boolean isCancelled() {
            return mIsCanceled;
        }

        @Override
        public boolean isDone() {
            return mIsDone;
        }

        @Override
        public Boolean get() throws InterruptedException, ExecutionException {
            try {

                for (Future<Boolean> childFuture : mChildren) {
                    boolean childSuccess = childFuture.get();
                    if (!childSuccess) {
                        return false;
                    }
                }

                return mExecutorService.submit(new Callable<Boolean>() {
                    @Override
                    public Boolean call() {
                        return mLocalTarget.run();
                    }
                }).get();

            } finally {
                mIsDone = true;
            }
        }

        @Override
        public Boolean get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
            try {

                for (Future<Boolean> childFuture : mChildren) {
                    boolean childSuccess = childFuture.get(timeout, unit);
                    if (!childSuccess) {
                        return false;
                    }
                }

                return mExecutorService.submit(new Callable<Boolean>() {
                    @Override
                    public Boolean call() {
                        return mLocalTarget.run();
                    }
                }).get(timeout, unit);

            } finally {
                mIsDone = true;
            }
        }
    }



}
