package com.devsmart.zookeeper.tasks

import java.nio.file.Path
import java.nio.file.Paths

class BasicTask implements BuildTask {

    String name
    Closure cmd
    final Set<String> dependcies = []
    final Set<Path> output = []

    def name(String name) {
        setName(name)
    }

    def depends(String taskName) {
        dependcies.add(taskName)
    }

    def depends(String... tasks) {
        dependcies.addAll(task)
    }

    def output(String filepath) {
        output.add(Paths.get(filepath))
    }

    def cmd(Closure cl) {
        setCmd(cl)
    }

    static BasicTask make(Closure cl) {
        BasicTask retval = new BasicTask()
        Closure code = cl.rehydrate(retval, retval, retval);
        code()
        return retval
    }

    @Override
    boolean run() {
        return false
    }
}
