package com.devsmart.zookeeper.artifacts

class PhonyArtifact implements Artifact {

    public final String name

    @Override
    String toString() {
        return "phony: " + name
    }

    @Override
    int hashCode() {
        return name.hashCode()
    }

    @Override
    boolean equals(Object o) {
        if(o == null || !(o instanceof PhonyArtifact)) {
            return false
        }

        return name.equalsIgnoreCase(o.name)
    }
}
