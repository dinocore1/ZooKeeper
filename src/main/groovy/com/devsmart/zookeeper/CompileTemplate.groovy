package com.devsmart.zookeeper

import com.devsmart.zookeeper.api.FileCollection

class CompileTemplate {

    String language
    Platform target
    Closure all
    Closure debug
    Closure release
    Closure cmd
    String workingDir

    def language(String str) {
        setLanguage(str)
    }

    def target(String str) {
        setTarget(Platform.parse(str))
    }

    def all(Closure cl) {
        setAll(cl)
    }

    def debug(Closure cl) {
        setDebug(cl)
    }

    def release(Closure cl) {
        setRelease(cl)
    }

    def cmd(Closure cl) {
        setCmd(cl)
    }

    def workingDir(String work) {
        setWorkingDir(work)
    }

    static CompileTemplate make(Closure cl, Project project) {
        CompileTemplate t = new CompileTemplate()
        Closure code = cl.rehydrate(t, project, t)
        code()
        return t
    }
}
