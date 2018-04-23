package com.devsmart.zookeeper.projectmodel

import com.devsmart.zookeeper.DependencyClosureDSL
import com.devsmart.zookeeper.LinkableLibrary
import com.devsmart.zookeeper.Version
import com.devsmart.zookeeper.api.FileCollection
import com.devsmart.zookeeper.file.FileUtils

class BuildableModule implements Module {

    String name
    Version version
    FileCollection src
    FileCollection includes = FileUtils.emptyFileCollection()
    LinkedHashSet<LinkableLibrary> dependencies = []
    LinkedHashSet<String> macrodefs = []
    LinkedHashMap<String, String> env = []

    @Override
    Set<Library> getDependencies() {
        return this.dependencies
    }

    void name(String name) {
        this.name = name
    }

    void version(String version) {
        this.version = Version.fromString(version)
    }

    void defs(String... defs) {
        macrodefs.addAll(defs)
    }

    void env(Map<String, String> map) {
        this.env.putAll(map)
    }

    void src(FileCollection files) {
        this.src = files
    }

    void include(FileCollection includes) {
        this.includes = includes
    }

    void dependencies(Closure cl) {
        DependencyClosureDSL delegate = new DependencyClosureDSL()
        cl.delegate = delegate
        cl.run()
        dependencies.addAll(delegate.call())
    }

}
