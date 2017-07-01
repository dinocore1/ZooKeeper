package com.devsmart.zookeeper.action;


import com.devsmart.zookeeper.*;
import com.google.common.base.Charsets;
import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;
import com.google.common.util.concurrent.Atomics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicReference;

public class ComputeBuildHash implements Action {

    private static final Logger LOGGER = LoggerFactory.getLogger(ComputeBuildHash.class);
    private static final HashFunction HASH_FUNCTION = Hashing.sha1();

    public static String createActionName(Library lib, Platform platform) {
        return "hash"+ Utils.captialFirstLetter(lib.name) + Utils.captialFirstLetter(platform.toString());
    }

    public File mSourceDir;
    public final TreeSet<String> mBuildParams = new TreeSet<String>();
    public final AtomicReference<HashCode> libraryHash = Atomics.newReference();

    @Override
    public void doIt() {
        try {
            FSChecksum fsChecksum = new FSChecksum(mSourceDir);
            HashCode fsHash = fsChecksum.computeHash();

            Hasher totalHasher = HASH_FUNCTION.newHasher();
            totalHasher.putBytes(fsHash.asBytes());

            for(String buildParam : mBuildParams) {
                totalHasher.putString(buildParam, Charsets.UTF_8);
            }

            libraryHash.set(totalHasher.hash());

        } catch (IOException e) {
            LOGGER.error("", e);
        }
    }
}
