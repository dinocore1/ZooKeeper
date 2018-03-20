package com.devsmart.zookeeper

class CompileTemplate {

    private CompileTemplate mParent

    private List<String> mFlags = []
    final List<File> includes = []

    final List<File> output = []
    final List<File> input = []

    private List mCommandLine

    CompileTemplate(CompileTemplate parent) {
        mParent = parent
    }

    CompileTemplate() {
    }

    def include(List<File> dirs) {
        mIncludes.addAll(dirs)
    }

    def flags(String... flags) {
        mFlags.addAll(flags)
    }

    def cmd(Object... cmd) {
        mCommandLine = cmd.flatten()
    }

    List getFlags() {
        if(mParent != null) {
            return mParent.flags + mFlags
        } else {
            return mFlags
        }
    }

    List getCmd() {
        if(mCommandLine == null && mParent != null) {
            return mParent.cmd
        } else {
            return mCommandLine
        }
    }

}


