package com.devsmart.zookeeper;

import com.devsmart.zookeeper.projectmodel.LinkType;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.Callable;

public class DependencyClosureDSL implements Callable<Set<LinkableLibrary>> {

    private LinkedHashSet<LinkableLibrary> mLibraries = new LinkedHashSet<>();

    void staticLib(String str) {
        DefaultLibrary lib = DefaultLibrary.parse(str);
        mLibraries.add(new LinkableLibrary(lib, LinkType.Static));
    }

    void sharedLib(String str) {
        DefaultLibrary lib = DefaultLibrary.parse(str);
        mLibraries.add(new LinkableLibrary(lib, LinkType.Dynamic));
    }

    @Override
    public Set<LinkableLibrary> call() {
        return mLibraries;
    }
}