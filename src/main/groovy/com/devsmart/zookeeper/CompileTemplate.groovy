package com.devsmart.zookeeper

class CompileTemplate {

    String language
    Platform target
    Closure debug
    Closure release
    Closure cmd

    def language(String str) {
        setLanguage(str)
    }

    def target(String str) {
        setTarget(Platform.parse(str))
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

    static CompileTemplate make(Closure cl) {
        CompileTemplate t = new CompileTemplate()
        Closure code = cl.rehydrate(t, t, t)
        code()
        return t
    }
}
