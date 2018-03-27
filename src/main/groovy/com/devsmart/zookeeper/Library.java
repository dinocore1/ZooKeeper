package com.devsmart.zookeeper;


import java.io.File;
import java.util.LinkedHashSet;

public class Library {

    public final String name;
    public final Version version;
    public final LinkedHashSet<File> includePaths = new LinkedHashSet<File>();
    public final LinkedHashSet<File> linkLibPaths = new LinkedHashSet<File>();


    public Library(String name, Version version) {
        this.name = name;
        this.version = version;
    }

    @Override
    public String toString() {
        return name + " " + version;
    }

    @Override
    public int hashCode() {
        return name.hashCode() ^ version.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == null || obj.getClass() != getClass()) {
            return false;
        }

        Library other = (Library) obj;
        return name.equals(other.name) && version.equals(other.version);
    }
}
