package com.devsmart.zookeeper

import com.devsmart.zookeeper.api.FileCollection
import com.devsmart.zookeeper.api.FileTree
import com.devsmart.zookeeper.file.DefaultBaseDirFileResolver
import com.devsmart.zookeeper.file.DefaultFileCollection
import com.devsmart.zookeeper.file.DefaultFileTree

class Project {

    private ZooKeeper mZooKeeper
    private File mProjectDir
    private DefaultBaseDirFileResolver mBaseDirFileResolver

    Project(File projectDir, ZooKeeper zooKeeper) {
        mProjectDir = projectDir
        mZooKeeper = zooKeeper
        mBaseDirFileResolver = new DefaultBaseDirFileResolver(mProjectDir)
    }

    File getProjectDir() {
        return mProjectDir
    }

    File file(Object obj) {

    }

    FileCollection files(Object... objects) {
        return new DefaultFileCollection("file collection", mBaseDirFileResolver, objects)
    }

    FileTree fileTree(Map<String, ?> args) {
        return new DefaultFileTree(args, mBaseDirFileResolver)

    }
}
