package com.devsmart.zookeeper;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.Callable;

public class DependencyClosureDSL implements Callable<Set<String>> {

    private LinkedHashSet<String> mStrings = new LinkedHashSet<String>();

    void lib(String str) {
        mStrings.add(str);
    }

    @Override
    public Set<String> call() {
        return mStrings;
    }
}