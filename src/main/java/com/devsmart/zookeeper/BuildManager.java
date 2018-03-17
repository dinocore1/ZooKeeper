package com.devsmart.zookeeper;


import com.devsmart.zookeeper.ast.Nodes;
import com.devsmart.zookeeper.tasks.*;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.TreeSet;
import java.util.regex.Pattern;

public class BuildManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(BuildManager.class);

    private static Pattern REGEX_FILE_SEP = Pattern.compile("/|\\\\");

    public ZooKeeper mZooKeeper;
    private List<CompilerConfig> mCompilerCfg = new ArrayList<>();

    private String toIncludeList(List<File> includeDirs) {
        StringBuilder builder = new StringBuilder();
        for(File f : includeDirs) {
            builder.append("-I");
            builder.append(f.getAbsolutePath());
            builder.append(" ");
        }

        return builder.toString();
    }

    public void addBuildLibrary(Nodes.BuildLibraryDefNode librayDef) {
        Platform platform = mZooKeeper.getNativeBuildPlatform();

        CompilerConfig config = mCompilerCfg.get(0);

        mZooKeeper.mVM.push();
        try {

            final File projectDir = new File(mZooKeeper.mVM.resolveVar(ZooKeeper.PROJECT_DIR));
            final File rootSrcDir = new File(projectDir, "src");

            File buildDir = new File(projectDir, "build");
            buildDir = new File(buildDir, platform.toString());
            buildDir = new File(buildDir, "debug");
            mZooKeeper.mVM.setVar(CompilerConfig.OUT_DIR, buildDir.getAbsolutePath());

            MkDirBuildTask mkDirBuildTask = new MkDirBuildTask(buildDir);
            mZooKeeper.mDependencyGraph.addTask(mkDirBuildTask);

            ProcessBuildTask linkerTask = new ProcessBuildTask();
            mZooKeeper.mDependencyGraph.addTask(linkerTask, "buildDebug");
            mZooKeeper.mDependencyGraph.addDependency(linkerTask, mkDirBuildTask);

            File exportIncludeDir = new File(projectDir, "include");

            //includes
            ArrayList<File> includeDirs = new ArrayList<File>();
            includeDirs.add(rootSrcDir);
            includeDirs.add(exportIncludeDir);

            String includeStr = toIncludeList(includeDirs);

            ArrayList<File> sourceFiles = new ArrayList<File>();
            findAllSrcFiles(sourceFiles, rootSrcDir);

            for(File srcFile : sourceFiles) {

                //Create compile task for each source
                mZooKeeper.mVM.push();
                try {

                    mZooKeeper.mVM.setVar(CompilerConfig.INPUT, srcFile.getAbsolutePath());
                    mZooKeeper.mVM.setVar(CompilerConfig.INCLUDES, includeStr);

                    final String srcFileStr = srcFile.toPath().toAbsolutePath().normalize().toString();
                    int i = commonPrefix(srcFileStr, rootSrcDir.getAbsolutePath());
                    String outputFilename = srcFileStr.substring(i+1);
                    outputFilename = REGEX_FILE_SEP.matcher(outputFilename).replaceAll("_");
                    outputFilename = outputFilename + ".o";

                    final File outputFile = new File(buildDir, outputFilename);
                    mZooKeeper.mVM.setVar(CompilerConfig.OUTPUT, outputFile.getAbsolutePath());

                    ProcessBuildTask compileTask = new ProcessBuildTask();

                    config.configCompileTask(compileTask, mZooKeeper, ImmutableList.of("debug", "sharedLib"));

                    mZooKeeper.mDependencyGraph.addTask(compileTask);
                    mZooKeeper.mDependencyGraph.addDependency(compileTask, mkDirBuildTask);
                    mZooKeeper.mDependencyGraph.addDependency(linkerTask, compileTask);

                    linkerTask.inputFiles.add(outputFile);


                } finally {
                    mZooKeeper.mVM.pop();
                }
            }

            File libFile;

            //Create Linker task
            mZooKeeper.mVM.push();
            try {
                mZooKeeper.mVM.setVar(CompilerConfig.OUTPUT_NAME, librayDef.libName);
                String libFilename = librayDef.libName + Platform.getLibraryExtention(platform);
                libFile = new File(buildDir, libFilename);

                mZooKeeper.mVM.setVar(CompilerConfig.OUTPUT, libFile.getAbsolutePath());

                mZooKeeper.mVM.setVar(CompilerConfig.INPUT, Joiner.on("").join(Lists.transform(linkerTask.inputFiles, new Function<File, String>() {
                    @Override
                    public String apply(File input) {
                        return input.getAbsolutePath();
                    }
                })));

                config.configLinkTask(linkerTask, mZooKeeper, ImmutableList.of("debug", "sharedLib"));


            } finally {
                mZooKeeper.mVM.pop();
            }

            //Create installer task
            PhonyBuildTask installLocal = new PhonyBuildTask();
            mZooKeeper.mDependencyGraph.addTask(installLocal, "installLocalDebug");
            mZooKeeper.mDependencyGraph.addDependency(installLocal, linkerTask);

            File localInstallDir = new File(mZooKeeper.mVM.resolveVar(ZooKeeper.ZOOKEEPER_HOME));
            localInstallDir = new File(localInstallDir, "install");
            localInstallDir = new File(localInstallDir, librayDef.libName);
            localInstallDir = new File(localInstallDir, platform.toString());
            localInstallDir = new File(localInstallDir, librayDef.versionNode.toString());

            MkDirBuildTask mkdir = new MkDirBuildTask(localInstallDir);
            mZooKeeper.mDependencyGraph.addTask(mkdir);

            CopyTask copyLibFile = new CopyTask(libFile, new File(localInstallDir, librayDef.libName + Platform.getLibraryExtention(platform)));
            mZooKeeper.mDependencyGraph.addTask(copyLibFile);
            mZooKeeper.mDependencyGraph.addDependency(copyLibFile, mkdir);
            mZooKeeper.mDependencyGraph.addDependency(installLocal, copyLibFile);

            CopyTask copyExportIncludeDir = new CopyTask(exportIncludeDir, new File(localInstallDir, "include"));
            mZooKeeper.mDependencyGraph.addTask(copyExportIncludeDir);
            mZooKeeper.mDependencyGraph.addDependency(installLocal, copyExportIncludeDir);


        } finally {
            mZooKeeper.mVM.pop();
        }
    }

    public void addBuildExe(Nodes.BuildExeDefNode exeDef) {
        Platform platform = mZooKeeper.getNativeBuildPlatform();

        CompilerConfig config = mCompilerCfg.get(0);

        mZooKeeper.mVM.push();
        try {

            final File projectDir = new File(mZooKeeper.mVM.resolveVar(ZooKeeper.PROJECT_DIR));
            final File rootSrcDir = new File(projectDir, "src");

            File buildDir = new File(projectDir, "build");
            buildDir = new File(buildDir, platform.toString());
            buildDir = new File(buildDir, "debug");

            MkDirBuildTask mkDirBuildTask = new MkDirBuildTask(buildDir);
            mZooKeeper.mDependencyGraph.addTask(mkDirBuildTask);

            ProcessBuildTask linkerTask = new ProcessBuildTask();
            mZooKeeper.mDependencyGraph.addTask(linkerTask, "buildDebug");
            mZooKeeper.mDependencyGraph.addDependency(linkerTask, mkDirBuildTask);

            ArrayList<File> sourceFiles = new ArrayList<File>();
            findAllSrcFiles(sourceFiles, rootSrcDir);

            for(File srcFile : sourceFiles) {

                //Create compile task for each source
                mZooKeeper.mVM.push();
                try {

                    mZooKeeper.mVM.setVar(CompilerConfig.INPUT, srcFile.getAbsolutePath());

                    final String srcFileStr = srcFile.toPath().toAbsolutePath().normalize().toString();
                    int i = commonPrefix(srcFileStr, rootSrcDir.getAbsolutePath());
                    String outputFilename = srcFileStr.substring(i+1);
                    outputFilename = REGEX_FILE_SEP.matcher(outputFilename).replaceAll("_");
                    outputFilename = outputFilename + ".o";

                    final File outputFile = new File(buildDir, outputFilename);
                    mZooKeeper.mVM.setVar(CompilerConfig.OUTPUT, outputFile.getAbsolutePath());

                    ProcessBuildTask compileTask = new ProcessBuildTask();

                    config.configCompileTask(compileTask, mZooKeeper, ImmutableList.of("debug", "exe"));

                    mZooKeeper.mDependencyGraph.addTask(compileTask);
                    mZooKeeper.mDependencyGraph.addDependency(compileTask, mkDirBuildTask);
                    mZooKeeper.mDependencyGraph.addDependency(linkerTask, compileTask);

                    linkerTask.inputFiles.add(outputFile);


                } finally {
                    mZooKeeper.mVM.pop();
                }
            }

            //Create Linker task
            mZooKeeper.mVM.push();
            try {
                mZooKeeper.mVM.setVar(CompilerConfig.OUTPUT_NAME, exeDef.exeName);
                File exeFile = new File(buildDir, exeDef.exeName);

                mZooKeeper.mVM.setVar(CompilerConfig.OUTPUT, exeFile.getAbsolutePath());

                mZooKeeper.mVM.setVar(CompilerConfig.INPUT, Joiner.on("").join(Lists.transform(linkerTask.inputFiles, new Function<File, String>() {
                    @Override
                    public String apply(File input) {
                        return input.getAbsolutePath();
                    }
                })));

                config.configLinkTask(linkerTask, mZooKeeper, ImmutableList.of("debug", "exe"));


            } finally {
                mZooKeeper.mVM.pop();
            }

        } finally {
            mZooKeeper.mVM.pop();
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

    public void addCompiler(JsonObject cfg) {
        mCompilerCfg.add(new CompilerConfig(cfg));
    }
}
