package com.devsmart.zookeeper.artifacts;

import java.io.File;

public class FileArtifact implements Artifact {

    public final File file;

    public FileArtifact(File f) {
        this.file = f;
    }

    @Override
    public String toString() {
        return "file: " + file.getPath();
    }

    @Override
    public int hashCode() {
        return file.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if(o == null || o.getClass() != FileArtifact.class) {
            return false;
        }

        return file.equals(((FileArtifact) o).file);
    }
}
