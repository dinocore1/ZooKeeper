package com.devsmart.zookeeper.tasks;


public interface BuildTask {

    /**
     * run this task.
     * @return true if this task ran sucessfully to completion, false otherwise
     */
    boolean run();
}
