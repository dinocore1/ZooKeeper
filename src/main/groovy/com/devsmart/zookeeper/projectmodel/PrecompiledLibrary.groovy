package com.devsmart.zookeeper.projectmodel

import com.devsmart.zookeeper.AbstractLibrary
import com.devsmart.zookeeper.DefaultLibrary
import com.devsmart.zookeeper.DependencyClosureDSL
import com.devsmart.zookeeper.Platform
import com.devsmart.zookeeper.Version

class PrecompiledLibrary extends AbstractLibrary implements Module {

    Platform platform
    LinkedHashSet<DefaultLibrary> dependencies = []

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
            dependencies.add(DefaultLibrary.parse(dependStr))
        }
    }
}
