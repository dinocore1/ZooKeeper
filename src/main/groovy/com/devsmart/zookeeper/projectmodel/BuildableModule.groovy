package com.devsmart.zookeeper.projectmodel

import com.devsmart.zookeeper.Version

import java.util.concurrent.Callable

class BuildableModule implements Module {

    String name
    Version version
    LinkedHashSet<AbstractLibrary> dependencies = []

    void name(String name) {
        this.name = name
    }

    void version(String version) {
        this.version = Version.fromString(version)
    }

    void dependencies(String... libs) {
        for(String dependStr : libs) {
            dependencies.add(AbstractLibrary.parse(dependStr))
        }
    }

    void dependencies(Collection<String> libs) {
        for(String dependStr : libs) {
            dependencies.add(AbstractLibrary.parse(dependStr))
        }
    }

    void dependencies(Closure cl) {
        DependencyClosureDSL delegate = new DependencyClosureDSL()
        cl.delegate = delegate
        cl.run()
        Set<String> result = delegate.call()
        dependencies(result)
    }

    private static class DependencyClosureDSL implements Callable<Set<String>> {

        private Set<String> mStrings = []

        void lib(String str) {
            mStrings.add(str)
        }

        @Override
        Set<String> call() {
            return mStrings
        }
    }

}
