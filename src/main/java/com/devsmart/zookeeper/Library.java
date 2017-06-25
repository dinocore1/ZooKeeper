package com.devsmart.zookeeper;


import java.io.File;
import java.util.LinkedHashSet;

public class Library {

    public final String name;
    public final Version version;
    public final LinkedHashSet<File> includePaths = new LinkedHashSet<File>();
    public final LinkedHashSet<File> linkLibPaths = new LinkedHashSet<File>();
    public final LinkedHashSet<String> linkLibraries = new LinkedHashSet<String>();


    public Library(String name, Version version) {
        this.name = name;
        this.version = version;
    }
}
