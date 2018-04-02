package com.devsmart.zookeeper.file;

import com.devsmart.zookeeper.PatternSet;
import com.devsmart.zookeeper.api.FileTree;

import java.io.File;
import java.util.Map;
import java.util.Set;

public class DefaultFileTree extends CompositFileTree {

    private File mBaseDir;
    private final PathToFileResolver mFileResolver;
    private PatternSet patternSet = new PatternSet();

    public DefaultFileTree(Map<String, ?> args, PathToFileResolver resolver) {
        mFileResolver = resolver;

        Object dir = args.get("dir");
        if(dir != null) {
            mBaseDir = mFileResolver.resolve(dir);
        } else {
            mBaseDir = mFileResolver.resolve(".");
        }

        Object include = args.get("include");
        if(include instanceof Iterable) {
            patternSet.include((Iterable)include);
        } else {
            patternSet.include((String) include);
        }

    }

    public Set<String> getIncludes() {
        return patternSet.getIncludes();
    }

    public FileTree setIncludes(Iterable<String> includes) {
        patternSet.setIncludes(includes);
        return this;
    }

    public Set<String> getExcludes() {
        return patternSet.getExcludes();
    }

    public FileTree setExcludes(Iterable<String> excludes) {
        patternSet.setExcludes(excludes);
        return this;
    }

    public FileTree include(String... includes) {
        patternSet.include(includes);
        return this;
    }

    public FileTree include(Iterable<String> includes) {
        patternSet.include(includes);
        return this;
    }

    public File getDir() {
        return mBaseDir;
    }

    @Override
    public void visitContents(FileCollectionResolveContext context) {
        File dir = getDir();
        context.add(new DirectoryFileTree(dir, patternSet));
    }

    @Override
    public String getDisplayName() {
        return "Directory '" + mBaseDir + "'";
    }
}
