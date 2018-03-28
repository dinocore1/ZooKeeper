package com.devsmart.zookeeper.tasks

import com.devsmart.zookeeper.FileCollection
import com.devsmart.zookeeper.FileUtils

class BasicTask implements BuildTask {

    String name
    Closure cmd
    final Set<String> dependencies = []
    FileCollection output

    def name(String name) {
        setName(name)
    }

    def depends(String taskName) {
        dependencies.add(taskName)
    }

    def depends(String... tasks) {
        dependencies.addAll(task)
    }

    def output(Object... paths) {
        output = FileUtils.from(paths)
    }

    def cmd(Closure cl) {
        setCmd(cl)
    }

    static BasicTask make(Closure cl) {
        BasicTask retval = new BasicTask()
        Closure code = cl.rehydrate(retval, retval, retval)
        code()
        return retval
    }

    @Override
    boolean run() {
        return false
    }
}
