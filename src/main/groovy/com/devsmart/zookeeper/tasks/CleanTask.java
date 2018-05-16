package com.devsmart.zookeeper.tasks;

import com.devsmart.zookeeper.ZooKeeper;
import com.devsmart.zookeeper.artifacts.Artifact;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Map;

public class CleanTask implements BuildTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(CleanTask.class);

    private final ZooKeeper mZooKeeper;

    public CleanTask(ZooKeeper zooKeeper) {
        mZooKeeper = zooKeeper;
    }

    @Override
    public boolean run() {

        for(Map.Entry<Artifact, BuildTask> entry : mZooKeeper.getArtifactMap().entrySet()){
            BuildTask task = entry.getValue();
            if(task instanceof BasicTask) {
                BasicTask basicTask = (BasicTask) task;
                for(File output : basicTask.getOutput()) {
                    LOGGER.info("deleting: {}", output);
                    output.delete();
                }
            }

        }

        return true;
    }
}
