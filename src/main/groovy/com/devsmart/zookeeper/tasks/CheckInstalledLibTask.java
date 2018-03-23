package com.devsmart.zookeeper.tasks;

import com.devsmart.zookeeper.CompilerContext;
import com.devsmart.zookeeper.Library;
import com.devsmart.zookeeper.Platform;
import com.devsmart.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

public class CheckInstalledLibTask implements BuildTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(CheckInstalledLibTask.class);

    public final ZooKeeper mZookeeper;
    public Library library;
    public Platform platform;
    private File mFoundLib;

    public CheckInstalledLibTask(ZooKeeper zooKeeper, Library depLib, Platform platform) {
        mZookeeper = zooKeeper;
        this.library = depLib;
        this.platform = platform;
    }

    @Override
    public boolean run() {

        if(mFoundLib == null) {


            File localInstallDir = mZookeeper.getLocalInstallDir(library, platform);
            File zooFile = new File(localInstallDir, "lib.zoo");

            if (zooFile.exists()) {
                try {
                    CompilerContext ctx = mZookeeper.createCompilerContext();
                    ctx.localDir = localInstallDir;
                    if(!mZookeeper.compileFile(zooFile, ctx)){
                        return false;
                    }

                    return true;
                } catch (IOException e) {
                    LOGGER.error("", e);
                    return false;
                }


            }

            return false;
        } else {
            return true;
        }
    }
}
