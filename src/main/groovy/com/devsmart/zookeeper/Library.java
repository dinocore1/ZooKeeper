package com.devsmart.zookeeper;


public class Library {

    public final String name;
    public final Version version;


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
