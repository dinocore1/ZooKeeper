package com.devsmart.zookeeper.action;

import com.devsmart.zookeeper.*;
import com.google.common.hash.HashCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;


public class VerifyLibraryInstalledAction implements Action {

    private static final Logger LOGGER = LoggerFactory.getLogger(VerifyLibraryInstalledAction.class);

    public static String createActionName(Library library, Platform platform) {
        return Utils.createActionName("verifyInstall", new LibraryPlatformKey(library, platform).toString());
    }

    public final Library library;
    public final Platform platform;
    private final ZooKeeper zooKeeper;
    public Action runIfNotInstalled;
    private boolean mIsInstalled;

    public VerifyLibraryInstalledAction(Library library, Platform platform, ZooKeeper zooKeeper) {
        this.library = library;
        this.platform = platform;
        this.zooKeeper = zooKeeper;
    }

    @Override
    public void doIt() {
        HashCode buildHash = zooKeeper.getBuildHash(library, platform);
        File installDir = zooKeeper.getInstallDir(library, platform, buildHash);

        mIsInstalled = installDir.exists() && installDir.isDirectory() && installDir.listFiles().length > 0;
        LOGGER.info("checking if {}-{} is installed...{}", library, platform, mIsInstalled);
        if(!mIsInstalled && runIfNotInstalled != null) {
            zooKeeper.mDependencyGraph.runAction(runIfNotInstalled);
        }

    }

}
