package com.devsmart.zookeeper.api;

import java.io.File;

public interface FileDetails {

    String getName();

    boolean isDirectory();

    File getFile();

    RelativePath getRelativePath();


}
