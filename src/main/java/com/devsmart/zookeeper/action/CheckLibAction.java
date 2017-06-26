package com.devsmart.zookeeper.action;

import com.devsmart.zookeeper.Action;
import com.devsmart.zookeeper.DependencyGraph;
import com.devsmart.zookeeper.Library;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;


public class CheckLibAction implements Action {

    private static final Logger LOGGER = LoggerFactory.getLogger(CheckLibAction.class);

    public Library library;
    public Action runIfNotFound;
    public DependencyGraph dependencyGraph;
    public File installDir;

    @Override
    public void doIt() {

        boolean isInstalled = installDir.exists();
        LOGGER.info("checking lib installed: {}...{}", library.name, isInstalled ? "YES" : "NO");
        if(!isInstalled) {
            dependencyGraph.runAction(runIfNotFound);
        }
    }
}
