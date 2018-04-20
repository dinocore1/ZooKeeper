package com.devsmart.zookeeper.tasks

import com.devsmart.zookeeper.api.FileCollection
import com.devsmart.zookeeper.file.FileUtils

class BasicTask {

    String name
    FileCollection output = FileUtils.emptyFileCollection()
    FileCollection input = FileUtils.emptyFileCollection()
    final List<Object> dependencies = []

    void name(String name) {
        setName(name)
    }

    void output(FileCollection fc) {
        output = fc
    }

    void input(FileCollection fc) {
        input = fc
    }

    void depends(String... objs) {
        dependencies.add(objs)
    }

}
