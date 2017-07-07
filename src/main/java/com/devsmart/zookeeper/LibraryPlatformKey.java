package com.devsmart.zookeeper;

import com.google.common.collect.ComparisonChain;


public class LibraryPlatformKey implements Comparable<LibraryPlatformKey> {

    public final Library lib;
    public final Platform platform;

    public LibraryPlatformKey(Library lib, Platform platform) {
        this.lib = lib;
        this.platform = platform;
    }

    @Override
    public int compareTo(LibraryPlatformKey o) {
        return ComparisonChain.start()
                .compare(lib.name, o.lib.name)
                .compare(lib.version, o.lib.version)
                .compare(platform.os, o.platform.os)
                .compare(platform.arch, o.platform.arch)
                .result();
    }

    @Override
    public String toString() {
        return String.format("%s-%s", lib.name, platform);
    }

    @Override
    public int hashCode() {
        return lib.hashCode() ^ platform.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || o.getClass() != getClass()) {
            return false;
        }

        LibraryPlatformKey other = (LibraryPlatformKey) o;
        return lib.equals(other.lib) && platform.equals(other.platform);
    }
}
