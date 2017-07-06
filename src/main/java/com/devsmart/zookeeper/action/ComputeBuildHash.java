package com.devsmart.zookeeper.action;


import com.devsmart.zookeeper.*;
import com.google.common.base.Charsets;
import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;
import com.google.common.io.BaseEncoding;
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

    public AtomicReference<File> mSourceDir;
    public final TreeSet<String> mBuildParams = new TreeSet<String>();
    public AtomicReference<HashCode> libraryHash;

    @Override
    public void doIt() throws Exception {
        FSChecksum fsChecksum = new FSChecksum(mSourceDir.get());
        HashCode fsHash = fsChecksum.computeHash();

        Hasher totalHasher = HASH_FUNCTION.newHasher();
        totalHasher.putBytes(fsHash.asBytes());

        for(String buildParam : mBuildParams) {
            totalHasher.putString(buildParam, Charsets.UTF_8);
        }

        final HashCode hashCode = totalHasher.hash();
        LOGGER.info("source checksum for {} : {}", mSourceDir, BaseEncoding.base16().encode(hashCode.asBytes()));

        if(libraryHash != null) {
            libraryHash.set(hashCode);
        }
    }
}
