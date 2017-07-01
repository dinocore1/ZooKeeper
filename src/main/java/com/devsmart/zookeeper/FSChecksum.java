package com.devsmart.zookeeper;


import com.google.common.hash.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;

public class FSChecksum {

    private static final Comparator<File> LEX_COMPARATOR = new Comparator<File>() {
        @Override
        public int compare(File o1, File o2) {
            return o1.getName().compareTo(o2.getName());
        }
    };

    public final File mRoot;
    private final HashFunction mHashFunction = Hashing.sha1();
    private Hasher mHasher;

    public FSChecksum(File root) {
        mRoot = root;
    }

    public HashCode computeHash() throws IOException {
        mHasher = mHashFunction.newHasher();
        computeHash(mRoot);
        return mHasher.hash();
    }

    private void computeHash(File f) throws IOException {
        if(f.isFile()) {
            computeFileHash(f);
        } else {
            File[] fileList = f.listFiles();
            Arrays.sort(fileList, LEX_COMPARATOR);
            for(File f1 : fileList) {
                computeHash(f);
            }
        }
    }

    private void computeFileHash(File f) throws IOException {
        FileInputStream fin = new FileInputStream(f);
        byte[] buffer = new byte[4096];
        int bytesRead;
        while((bytesRead = fin.read(buffer, 0, buffer.length)) > 0) {
            mHasher.putBytes(buffer, 0, bytesRead);
        }
    }
}
