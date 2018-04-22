package com.devsmart.zookeeper;

import com.devsmart.zookeeper.projectmodel.Library;

public abstract class AbstractLibrary implements Library {

    protected String name;
    protected Version version;

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Version getVersion() {
        return version;
    }

    public static int libraryHashCode(Library lib) {
        return lib.getName().hashCode() ^ lib.getVersion().hashCode();
    }

    public static boolean libraryEquals(Library a, Library b) {
        return a.getName().equals(b.getName()) &&
                a.getVersion().equals(b.getVersion());
    }

    @Override
    public int hashCode() {
        return libraryHashCode(this);
    }

    @Override
    public boolean equals(Object o) {
        if(o == null) {
            return false;
        }
        if(o instanceof Library) {
            return libraryEquals(this, (Library)o);
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        return String.format("%s:%s", name, version);
    }
}
