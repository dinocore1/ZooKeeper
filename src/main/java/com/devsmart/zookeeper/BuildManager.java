package com.devsmart.zookeeper;


import com.devsmart.zookeeper.ast.Nodes;
import com.google.common.base.Throwables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Path;
import java.util.Collection;

public class BuildManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(BuildManager.class);

    public ZooKeeper mZooKeeper;

    public void addBuildLibrary(Nodes.BuildLibraryDefNode librayDef) {

        try {

            BuildLibrary buildLibrary = new BuildLibrary(librayDef.libName, librayDef.versionNode.version);

            final File projectDir = new File(mZooKeeper.mVM.resolveVar(ZooKeeper.PROJECT_DIR));
            final File rootSrcDir = new File(projectDir, "src");
            final File buildDir = new File(projectDir, "build");
            librayDef.objectNode.entries.add("src", new Nodes.StringNode(rootSrcDir.getCanonicalPath()));
            findAllSrcFiles(buildLibrary.sourceFiles, librayDef.objectNode.get("src"));

            for(File srcFile : buildLibrary.sourceFiles) {

                final String srcFileStr = srcFile.toPath().toAbsolutePath().normalize().toString();
                int i = commonPrefix(srcFileStr, rootSrcDir.getAbsolutePath());
                String outputPath = srcFileStr.substring(i);

                File outputFile = new File(buildDir, outputPath + ".o");

                //Action compileAction = compiler.createCompileToObjectAction(srcFile);



            }


        } catch (Exception e) {
            LOGGER.error("", e);
            Throwables.propagate(e);
        }
    }

    private int commonPrefix(String a, String b) {
        int i=0;
        for(;i<Math.min(a.length(), b.length());i++) {
            if(a.charAt(i) != b.charAt(i)) {
                return i;
            }
        }
        return i;
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
        if(rootDir.exists()) {
            if(rootDir.isDirectory()) {
                for(File f : rootDir.listFiles()) {
                    findAllSrcFiles(sourceFiles, f);
                }
            } else if(rootDir.isFile() && rootDir.getName().endsWith(".cpp")){
                sourceFiles.add(rootDir);
            }
        }

    }
}
