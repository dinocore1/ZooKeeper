package com.devsmart.zookeeper;

import com.devsmart.zookeeper.api.FileTreeElement;
import com.devsmart.zookeeper.api.Spec;
import com.devsmart.zookeeper.specs.PatternSpecFactory;
import com.devsmart.zookeeper.specs.Specs;
import com.google.common.collect.Sets;
import groovy.lang.Closure;

import java.util.Collections;
import java.util.Set;

public class PatternSet {

    private Set<String> includes;
    private Set<String> excludes;
    private Set<Spec<FileTreeElement>> includeSpecs;
    private Set<Spec<FileTreeElement>> excludeSpecs;

    public PatternSet include(String... includes) {
        Collections.addAll(getIncludes(), includes);
        return this;
    }

    public PatternSet include(Closure closure) {
        include(Specs.<FileTreeElement>convertClosureToSpec(closure));
        return this;
    }

    public PatternSet include(Spec<FileTreeElement> spec) {
        getIncludeSpecs().add(spec);
        return this;
    }

    public PatternSet setIncludes(Iterable<String> includes) {
        this.includes = null;
        return include(includes);
    }

    public PatternSet include(Iterable includes) {
        for (Object include : includes) {
            getIncludes().add((String) include);
        }
        return this;
    }

    public void setExcludes(Iterable<String> excludes) {

    }

    public Set<String> getExcludes() {
        if (excludes == null) {
            excludes = Sets.newLinkedHashSet();
        }
        return excludes;
    }

    public Set<String> getIncludes() {
        if (includes == null) {
            includes = Sets.newLinkedHashSet();
        }
        return includes;
    }

    public Set<Spec<FileTreeElement>> getIncludeSpecs() {
        if (includeSpecs == null) {
            includeSpecs = Sets.newLinkedHashSet();
        }
        return includeSpecs;
    }

    public Set<Spec<FileTreeElement>> getExcludeSpecs() {
        if (excludeSpecs == null) {
            excludeSpecs = Sets.newLinkedHashSet();
        }
        return excludeSpecs;
    }

    public boolean isCaseSensitive() {
        return false;
    }

    public Spec<FileTreeElement> getAsSpec() {
        return PatternSpecFactory.INSTANCE.createSpec(this);
    }




}
