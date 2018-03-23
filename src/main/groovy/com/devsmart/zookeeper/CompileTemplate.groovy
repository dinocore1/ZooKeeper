package com.devsmart.zookeeper

class CompileTemplate {

    protected CompileTemplate mParent

    private String mLanguage = ''
    private List<String> mFlags = []
    private List<File> mStaticIncludes = []
    private List mCommandLine

    CompileTemplate(CompileTemplate parent) {
        mParent = parent
    }

    CompileTemplate(CompileTemplate copy, CompileTemplate parent) {
        mFlags = copy.mFlags
        mStaticIncludes = copy.mStaticIncludes
        mCommandLine = copy.mCommandLine
        mParent = parent
    }

    CompileTemplate() {
    }

    def language(String language) {
        mLanguage = language
    }

    def includes(String... includes) {
        mStaticIncludes.addAll(includes.collect({new File(it)}))
    }

    def flags(String... flags) {
        mFlags.addAll(flags)
    }

    def flags(String flag) {
        mFlags.add(flag)
    }

    def cmd(Object... cmd) {
        mCommandLine = cmd.flatten()
    }

    Closure<File> getOutput() {
        return { CompileTarget t -> t.output }
    }

    Closure<File> getInput() {
        return { CompileTarget t -> t.input }
    }

    Closure<List<File>> getIncludes() {
        return { CompileTarget t -> mStaticIncludes + t.includes }
    }

    Closure<List<String>> getFlags() {
        return { CompileTarget t -> flagsRecurseive() + t.flags }
    }

    private List flagsRecurseive() {
        if(mParent != null) {
            return mParent.flagsRecurseive() + mFlags
        } else {
            return mFlags
        }
    }

    private List cmdRecursive() {
        if(mCommandLine == null && mParent != null) {
            return mParent.cmdRecursive()
        } else {
            return mCommandLine
        }
    }

    List getCmdLine(CompileTarget target) {
        List l = cmdRecursive()
        final CompileTemplate t = this
        return l.flatten {
            if(it instanceof Closure) {
                def code = it.rehydrate(t, t, t)
                return code(target)
            } else {
                return it
            }
        }

    }

}

