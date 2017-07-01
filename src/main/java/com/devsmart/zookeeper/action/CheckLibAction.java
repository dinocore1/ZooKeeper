package com.devsmart.zookeeper.action;

import com.devsmart.zookeeper.Action;
import com.devsmart.zookeeper.DependencyGraph;
import com.devsmart.zookeeper.Library;
import com.google.common.hash.HashCode;
import com.google.common.io.BaseEncoding;
import com.google.common.util.concurrent.Atomics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;


public class CheckLibAction implements Action {

    private static final Logger LOGGER = LoggerFactory.getLogger(CheckLibAction.class);

    public final AtomicBoolean libraryInstalled = new AtomicBoolean();
    public final AtomicReference<HashCode> libraryHash = Atomics.newReference();

    public File cacheDir;

    @Override
    public void doIt() {

        /*
        HashCode libHash = libraryHash.get();
        String hashStr = BaseEncoding.base16().encode(libHash.asBytes());
        hashStr = hashStr.substring(0, 7);

        LOGGER.info("checking for lib: {}", hashStr);

        boolean isInstalled = installDir.exists() && installDir.listFiles().length > 0;
        LOGGER.info("checking lib installed: {}...{}", library.name, isInstalled ? "YES" : "NO");
        if(!isInstalled) {
            dependencyGraph.runAction(runIfNotFound);
        }
        */
    }
}
