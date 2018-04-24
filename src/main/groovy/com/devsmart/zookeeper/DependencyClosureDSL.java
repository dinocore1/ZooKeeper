package com.devsmart.zookeeper;

import com.devsmart.zookeeper.projectmodel.Library;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.Callable;

public class DependencyClosureDSL implements Callable<Set<Library>> {

    private LinkedHashSet<Library> mLibraries = new LinkedHashSet<>();

    void lib(String str) {
        DefaultLibrary lib = DefaultLibrary.parse(str);
        mLibraries.add(lib);
    }

    @Override
    public Set<Library> call() {
        return mLibraries;
    }
}