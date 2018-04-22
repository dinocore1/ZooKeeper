package com.devsmart.zookeeper.projectmodel

import com.devsmart.zookeeper.DefaultLibrary
import com.devsmart.zookeeper.DependencyClosureDSL
import com.devsmart.zookeeper.Version
import com.devsmart.zookeeper.api.FileCollection
import com.devsmart.zookeeper.file.FileUtils

class BuildableModule implements Module {

    String name
    Version version
    FileCollection src
    FileCollection includes = FileUtils.emptyFileCollection()
    LinkedHashSet<DefaultLibrary> dependencies = []
    LinkedHashSet<String> macrodefs = []

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
        for(String dependStr : delegate.call()) {
            dependencies.add(DefaultLibrary.parse(dependStr))
        }
    }

}
