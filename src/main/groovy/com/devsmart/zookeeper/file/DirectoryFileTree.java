package com.devsmart.zookeeper.file;

import com.devsmart.zookeeper.PatternSet;
import com.devsmart.zookeeper.api.FileDetails;
import com.devsmart.zookeeper.api.FileTree;
import com.devsmart.zookeeper.api.FileVisitor;
import com.devsmart.zookeeper.api.RelativePath;

import java.io.File;
import java.util.concurrent.atomic.AtomicBoolean;

public class DirectoryFileTree implements MinFileTree {


    private final File mBaseDir;
    private Object mSpec;

    public DirectoryFileTree(File baseDir, PatternSet patternSet) {
        mBaseDir = baseDir;
        //mSpec = patternSet.getAsSpec();
    }

    @Override
    public void visit(FileVisitor visitor) {
        visitFrom(visitor, mBaseDir, RelativePath.EMPTY_ROOT);
    }

    private void visitFrom(FileVisitor visitor, File fileOrDir, RelativePath path) {
        if(fileOrDir.isFile()) {
            path = path.append(true, fileOrDir.getName());
            FileDetails detail = new DefaultFileDetails(fileOrDir, path);
            visitor.visit(detail);
        } else if(fileOrDir.isDirectory()) {
            path = path.append(false, fileOrDir.getName());
            FileDetails detail = new DefaultFileDetails(fileOrDir, path);
            visitor.visit(detail);
            for(File f : fileOrDir.listFiles()) {
                visitFrom(visitor, f, path);
            }
        }

    }

    private static class DefaultFileDetails implements FileDetails {

        private final File mFile;
        private final RelativePath mRelitivePath;

        public DefaultFileDetails(File fileOrDir, RelativePath path) {
            mFile = fileOrDir;
            mRelitivePath = path;
        }

        @Override
        public String getName() {
            return mFile.getName();
        }

        @Override
        public boolean isDirectory() {
            return mFile.isDirectory();
        }

        @Override
        public File getFile() {
            return mFile;
        }

        @Override
        public RelativePath getRelitivePath() {
            return mRelitivePath;
        }
    }


}
