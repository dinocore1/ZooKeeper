package com.devsmart.zookeeper;


import java.io.File;
import java.util.LinkedHashSet;

public class BuildLibrary extends Library {

    public final LinkedHashSet<File> sourceFiles = new LinkedHashSet<File>();

    public BuildLibrary(String name, Version version) {
        super(name, version);
    }
}
