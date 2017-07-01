package com.devsmart.zookeeper.action;


import com.devsmart.zookeeper.Action;
import com.devsmart.zookeeper.FSChecksum;
import com.google.common.base.Charsets;
import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

public class ComputeBuildHash implements Action {

    private static final Logger LOGGER = LoggerFactory.getLogger(ComputeBuildHash.class);

    private static final HashFunction HASH_FUNCTION = Hashing.sha1();

    public File mSourceDir;
    public final TreeMap<String, String> mBuildParams = new TreeMap<String, String>();
    private HashCode mHash;

    public HashCode getHashCode() {
        return mHash;
    }

    @Override
    public void doIt() {
        try {
            FSChecksum fsChecksum = new FSChecksum(mSourceDir);
            HashCode fsHash = fsChecksum.computeHash();

            Hasher totalHasher = HASH_FUNCTION.newHasher();
            totalHasher.putBytes(fsHash.asBytes());

            for(Map.Entry<String, String> buildParam : mBuildParams.entrySet()) {
                totalHasher.putString(buildParam.getKey(), Charsets.UTF_8);
                totalHasher.putString(buildParam.getValue(), Charsets.UTF_8);
            }

            mHash = totalHasher.hash();

        } catch (IOException e) {
            LOGGER.error("", e);
        }
    }
}
