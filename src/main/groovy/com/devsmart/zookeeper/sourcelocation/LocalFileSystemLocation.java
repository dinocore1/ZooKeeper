package com.devsmart.zookeeper.sourcelocation;

import com.devsmart.zookeeper.SourceLocation;

import java.io.File;


public class LocalFileSystemLocation implements SourceLocation {

    public final File mSourceFolder;

    public LocalFileSystemLocation(File srcDir) {
        mSourceFolder = srcDir;
    }
}
