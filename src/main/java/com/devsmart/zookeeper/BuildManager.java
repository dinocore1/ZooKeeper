package com.devsmart.zookeeper;


import com.devsmart.zookeeper.ast.Nodes;
import com.devsmart.zookeeper.tasks.BuildArtifact;
import com.devsmart.zookeeper.tasks.MkDirBuildTask;
import com.devsmart.zookeeper.tasks.ProcessBuildTask;
import com.google.common.base.Throwables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.regex.Pattern;

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

    private static Pattern REGEX_FILE_SEP = Pattern.compile("/|\\\\");

    public void addBuildExe(Nodes.BuildExeDefNode exeDef) {
        Platform platform = mZooKeeper.getNativeBuildPlatform();

        final File projectDir = new File(mZooKeeper.mVM.resolveVar(ZooKeeper.PROJECT_DIR));
        final File rootSrcDir = new File(projectDir, "src");

        File buildDir = new File(projectDir, "build");
        buildDir = new File(buildDir, platform.toString());
        buildDir = new File(buildDir, "debug");

        File exeFile = new File(buildDir, exeDef.exeName);

        ProcessBuildTask linkTask = new ProcessBuildTask();
        linkTask.commandLine.add("c++");
        linkTask.commandLine.add("-o");
        linkTask.commandLine.add(exeFile.getAbsolutePath());
        linkTask.outputFiles.add(exeFile);

        mZooKeeper.mDependencyGraph.addTask(linkTask, "buildDebug");

        ArrayList<File> sourceFiles = new ArrayList<File>();
        findAllSrcFiles(sourceFiles, rootSrcDir);

        for(File srcFile : sourceFiles) {

            ProcessBuildTask buildTask = new ProcessBuildTask();
            buildTask.inputFiles.add(srcFile);



            final String srcFileStr = srcFile.toPath().toAbsolutePath().normalize().toString();
            int i = commonPrefix(srcFileStr, rootSrcDir.getAbsolutePath());
            String outputFilename = srcFileStr.substring(i+1);
            outputFilename = REGEX_FILE_SEP.matcher(outputFilename).replaceAll("_");
            outputFilename = outputFilename + ".o";


            final File outputFile = new File(buildDir, outputFilename);

            buildTask.outputFiles.add(outputFile);

            buildTask.commandLine.add("c++");
            buildTask.commandLine.add("-o");
            buildTask.commandLine.add(outputFile.getAbsolutePath());
            buildTask.commandLine.add("-c");
            buildTask.commandLine.add(srcFile.getAbsolutePath());

            mZooKeeper.mDependencyGraph.addTask(buildTask);

            MkDirBuildTask mkOutputDirTask = new MkDirBuildTask(outputFile.getParentFile());
            mZooKeeper.mDependencyGraph.addTask(mkOutputDirTask);
            mZooKeeper.mDependencyGraph.addDependency(buildTask, mkOutputDirTask);

            linkTask.inputFiles.add(outputFile);
            mZooKeeper.mDependencyGraph.addDependency(linkTask, buildTask);
        }


        for(File objFile : linkTask.inputFiles) {
            linkTask.commandLine.add(objFile.getAbsolutePath());
        }

    }

    private int commonPrefix(String a, String b) {
        int i = 0;
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
