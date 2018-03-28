package com.devsmart.zookeeper.artifacts

class FileArtifact implements Artifact {

    public final File file

    FileArtifact(File f) {
        this.file = f
    }

    @Override
    String toString() {
        return "file: " + file.absolutePath
    }

    @Override
    int hashCode() {
        return file.hashCode()
    }

    @Override
    boolean equals(Object o) {
        if(o == null || !(o instanceof FileArtifact)) {
            return false
        }

        return file.equals(o.file)
    }
}
