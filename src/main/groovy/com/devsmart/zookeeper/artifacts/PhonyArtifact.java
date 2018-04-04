package com.devsmart.zookeeper.artifacts;

public class PhonyArtifact implements Artifact {

    public final String name;

    public PhonyArtifact(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "phony: " + name;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if(o == null || o.getClass() != PhonyArtifact.class) {
            return false;
        }

        return name.equalsIgnoreCase(((PhonyArtifact)o).name);
    }
}
