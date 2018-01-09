package com.devsmart.zookeeper;


import com.devsmart.zookeeper.ast.Nodes;
import com.google.common.base.Throwables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Collection;

public class BuildManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(BuildManager.class);

    public VM mVM;
    public DependencyGraph mDependencyGraph;

    public void addBuildLibrary(Nodes.BuildLibraryDefNode librayDef) {

        try {

            BuildLibrary buildLibrary = new BuildLibrary(librayDef.libName, librayDef.versionNode.version);
            File rootSrcDir = new File(".");
            rootSrcDir = new File(rootSrcDir, "src");
            librayDef.objectNode.entries.add("src", new Nodes.StringNode(rootSrcDir.getCanonicalPath()));
            findAllSrcFiles(buildLibrary.sourceFiles, librayDef.objectNode.get("src"));

            mDependencyGraph.addAction();

        } catch (Exception e) {
            LOGGER.error("", e);
            Throwables.propagate(e);
        }
    }

    private void findAllSrcFiles(Collection<File> sourceFiles, Iterable<Nodes.ValueNode> values) {
        for(Nodes.ValueNode srcDir : values){
            if(srcDir.isString()) {
                findAllSrcFiles(sourceFiles, new File(srcDir.toString()));
            } else if(srcDir.isArray()) {
                findAllSrcFiles(sourceFiles, ((Nodes.ArrayNode) srcDir).array);
            }
        }
    }

    private void findAllSrcFiles(Collection<File> sourceFiles, File rootDir) {
        for(File f : rootDir.listFiles()) {
            if(f.isFile() && f.getName().endsWith(".cpp")) {
                sourceFiles.add(f);
            } else if(f.isDirectory()) {
                findAllSrcFiles(sourceFiles, f);
            }
        }
    }
}
