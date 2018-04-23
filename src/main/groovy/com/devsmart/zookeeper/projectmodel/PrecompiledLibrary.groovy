package com.devsmart.zookeeper.projectmodel

import com.devsmart.zookeeper.AbstractLibrary
import com.devsmart.zookeeper.DefaultLibrary
import com.devsmart.zookeeper.DependencyClosureDSL
import com.devsmart.zookeeper.LinkableLibrary
import com.devsmart.zookeeper.Platform
import com.devsmart.zookeeper.Version
import com.devsmart.zookeeper.api.FileCollection
import com.devsmart.zookeeper.file.FileUtils

class PrecompiledLibrary extends AbstractLibrary implements Module {

    Platform platform
    LinkedHashSet<LinkableLibrary> dependencies = []
    FileCollection includes = FileUtils.emptyFileCollection()
    LinkedHashSet<String> macrodefs = []
    LinkedHashMap<String, String> env = []
    FileCollection sharedLib
    FileCollection staticLib

    void name(String name) {
        this.name = name
    }

    void version(String version) {
        this.version = Version.fromString(version)
    }

    void platform(String platform) {
        this.platform = Platform.parse(platform)
    }

    void defs(String... defs) {
        macrodefs.addAll(defs)
    }

    void env(Map<String, String> map) {
        this.env.putAll(map)
    }

    void include(FileCollection includes) {
        this.includes = includes
    }

    void sharedLib(FileCollection sharedLib) {
        this.sharedLib = sharedLib
    }

    void staticLib(FileCollection staticLib) {
        this.staticLib = staticLib
    }

    void dependencies(Closure cl) {
        DependencyClosureDSL delegate = new DependencyClosureDSL()
        cl.delegate = delegate
        cl.run()
        dependencies.addAll(delegate.call())
    }
}
