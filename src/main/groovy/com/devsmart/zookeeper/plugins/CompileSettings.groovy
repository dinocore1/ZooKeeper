package com.devsmart.zookeeper.plugins

import com.devsmart.zookeeper.DefaultLibrary
import com.devsmart.zookeeper.tasks.CompileChildProcessTask

class CompileSettings implements CompileProcessModifier {

    final LinkedHashSet<String> flags = []
    final LinkedHashSet<File> includes = []
    final LinkedHashSet<DefaultLibrary> staticLinkedLibs = []
    final LinkedHashSet<DefaultLibrary> sharedLinkedLibs = []
    final LinkedHashSet<String> macrodefines = []
    final LinkedHashMap<String, String> env = [:]

    @Override
    void apply(CompileChildProcessTask ctx) {
        ctx.compileContext.flags.addAll(flags)
        ctx.compileContext.includes.addAll(includes)
        ctx.compileContext.staticLinkedLibs.addAll(staticLinkedLibs)
        ctx.compileContext.sharedLinkedLibs.addAll(sharedLinkedLibs)
        ctx.compileContext.macrodefines.addAll(macrodefines)
        ctx.compileContext.env.putAll(env)
    }
}
