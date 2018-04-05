package com.devsmart.zookeeper.tasks

import com.devsmart.zookeeper.api.FileCollection

class BasicTask {

    String name
    FileCollection output
    FileCollection input
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
