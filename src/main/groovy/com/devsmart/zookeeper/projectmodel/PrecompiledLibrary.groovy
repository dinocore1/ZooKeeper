package com.devsmart.zookeeper.projectmodel

import com.devsmart.zookeeper.DependencyClosureDSL
import com.devsmart.zookeeper.Platform
import com.devsmart.zookeeper.Version

class PrecompiledLibrary implements Library {

    String name
    Version version
    Platform platform
    LinkedHashSet<AbstractLibrary> dependencies = []

    void name(String name) {
        this.name = name
    }

    void version(String version) {
        this.version = Version.fromString(version)
    }

    void platform(String platform) {
        this.platform = Platform.parse(platform);
    }

    void dependencies(Closure cl) {
        DependencyClosureDSL delegate = new DependencyClosureDSL()
        cl.delegate = delegate
        cl.run()
        for(String dependStr : delegate.call()) {
            dependencies.add(AbstractLibrary.parse(dependStr))
        }
    }
}
