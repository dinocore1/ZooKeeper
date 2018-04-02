package com.devsmart.zookeeper.tasks

import com.devsmart.zookeeper.api.FileCollection
import com.devsmart.zookeeper.Version

class GenericBuildTask extends BasicTask {

    Version version = Version.fromString('0.0.0')
    FileCollection sources

    def version(String versionstr) {
        setVersion(Version.fromString(versionstr))
    }

    def src(FileCollection srcFiles) {
        setSources(srcFiles)
    }

    @Override
    String toString() {
        return "build: " + name
    }
}
