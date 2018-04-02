package com.devsmart.zookeeper.file;

import com.devsmart.zookeeper.PatternSet;
import com.devsmart.zookeeper.api.*;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.atomic.AtomicBoolean;

public class DirectoryFileTree implements MinFileTree {


    private final File mBaseDir;
    private Spec<FileTreeElement> mSpec;

    public DirectoryFileTree(File baseDir, PatternSet patternSet) {
        mBaseDir = baseDir;
        mSpec = patternSet.getAsSpec();
    }

    @Override
    public void visit(FileVisitor visitor) {
        visitFrom(visitor, mBaseDir, RelativePath.EMPTY_ROOT);
    }

    private void visitFrom(FileVisitor visitor, File fileOrDir, RelativePath path) {
        if(fileOrDir.isFile()) {
            path = path.append(true, fileOrDir.getName());
            DefaultFileDetails detail = new DefaultFileDetails(fileOrDir, path);
            if(mSpec.isSatisfiedBy(detail)) {
                visitor.visit(detail);
            }
        } else if(fileOrDir.isDirectory()) {
            path = path.append(false, fileOrDir.getName());
            DefaultFileDetails detail = new DefaultFileDetails(fileOrDir, path);
            if(mSpec.isSatisfiedBy(detail)) {
                visitor.visit(detail);
                for (File f : fileOrDir.listFiles()) {
                    visitFrom(visitor, f, path);
                }
            }
        }

    }

    private static class DefaultFileDetails implements FileDetails, FileTreeElement {

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
        public String getPath() {
            return null;
        }

        @Override
        public int getMode() {
            return 0;
        }

        @Override
        public boolean isDirectory() {
            return mFile.isDirectory();
        }

        @Override
        public long getLastModified() {
            return 0;
        }

        @Override
        public long getSize() {
            return 0;
        }

        @Override
        public InputStream open() {
            return null;
        }

        @Override
        public void copyTo(OutputStream output) {

        }

        @Override
        public boolean copyTo(File target) {
            return false;
        }

        @Override
        public File getFile() {
            return mFile;
        }

        @Override
        public RelativePath getRelativePath() {
            return mRelitivePath;
        }

    }


}
