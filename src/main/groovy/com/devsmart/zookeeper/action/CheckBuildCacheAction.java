package com.devsmart.zookeeper.action;

import com.devsmart.zookeeper.Action;
import com.devsmart.zookeeper.Library;
import com.devsmart.zookeeper.Platform;
import com.devsmart.zookeeper.Utils;
import com.google.common.hash.HashCode;
import com.google.common.io.BaseEncoding;
import com.google.common.util.concurrent.Atomics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;


public class CheckBuildCacheAction implements Action {

    private static final Logger LOGGER = LoggerFactory.getLogger(CheckBuildCacheAction.class);

    public final AtomicBoolean libraryInstalled = new AtomicBoolean();
    public AtomicReference<HashCode> libraryHash;

    public File cacheDir;

    public static File createCacheFile(File cacheRoot, HashCode libHash) {
        String hashStr = BaseEncoding.base16().encode(libHash.asBytes());
        hashStr = hashStr.substring(0, 7);

        File cacheFile = new File(cacheRoot, hashStr);
        return cacheFile;
    }

    public static String createActionName(Library library, Platform platform) {
        return "check" + Utils.captialFirstLetter(library.name) + Utils.captialFirstLetter(platform.toString());
    }

    @Override
    public void doIt() {
        HashCode libHash = libraryHash.get();
        File cacheFile = createCacheFile(cacheDir, libHash);
        libraryInstalled.set(cacheFile.exists());
    }


}
