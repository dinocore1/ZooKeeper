package com.devsmart.zookeeper;


import com.devsmart.StringUtils;
import com.devsmart.zookeeper.action.*;
import com.devsmart.zookeeper.ast.Nodes;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.hash.HashCode;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.regex.Pattern;

public class SemPass2 extends ZooKeeperBaseVisitor<Void> {

    private static final Pattern URL_REGEX = Pattern.compile("^https?://");

    static final String KEY_CMAKE_ARGS = "cmake_args";

    static final ImmutableList<String> BUILD_ARGS;

    static {
        BUILD_ARGS = ImmutableList.of(KEY_CMAKE_ARGS);
    }

    private final CompilerContext mContext;

    public SemPass2(CompilerContext compilerContext) {
        mContext = compilerContext;
    }

    @Override
    public Void visitLibrary(ZooKeeperParser.LibraryContext ctx) {
        List<Action> preBuildDependencies = new ArrayList<Action>();
        final Nodes.LibNode libNode = (Nodes.LibNode) mContext.nodeMap.get(ctx);

        final Platform platform = mContext.zooKeeper.getBuildPlatform();

        //Compute Build Hash
        final ComputeBuildHash buildHashAction = new ComputeBuildHash(libNode.library, platform, mContext.zooKeeper);
        mContext.dependencyGraph.addAction(ComputeBuildHash.createActionName(libNode.library, platform), buildHashAction);
        preBuildDependencies.add(buildHashAction);

        //Check for installed Build
        final CheckBuildInstalledAction checkBuildInstalledAction = new CheckBuildInstalledAction(libNode.library, platform, mContext.zooKeeper);
        mContext.dependencyGraph.addAction(CheckBuildInstalledAction.createActionName(libNode.library, platform), checkBuildInstalledAction);
        mContext.dependencyGraph.addDependency(checkBuildInstalledAction, buildHashAction);


        File sourceDir;
        if(StringUtils.isEmptyString(libNode.src)){
            sourceDir = new File("").getAbsoluteFile();
        } else if(URL_REGEX.matcher(libNode.src).find()){
            String httpUrl = libNode.src;
            sourceDir = new File(mContext.fileRoot, "download");
            sourceDir = new File(sourceDir, libNode.library.name+libNode.library.version);
            DownloadAndUnzipAction downloadAction = new DownloadAndUnzipAction(httpUrl, sourceDir);
            mContext.dependencyGraph.addAction(DownloadAndUnzipAction.createActionName(libNode.library), downloadAction);
            preBuildDependencies.add(downloadAction);
            mContext.dependencyGraph.addDependency(buildHashAction, downloadAction);
        } else {
            sourceDir = new File(libNode.src);
            if(!sourceDir.exists()) {
                mContext.error("source dir does not exist: " + sourceDir.getAbsolutePath(), null);
            }
        }

        buildHashAction.mSourceDir = sourceDir;

        BuildCMakeLibAction cmakeBuildAction = new BuildCMakeLibAction();
        checkBuildInstalledAction.runIfNotInstalled = cmakeBuildAction;
        cmakeBuildAction.rootDir = sourceDir;
        cmakeBuildAction.installDirCallable = new Callable<File>() {
            @Override
            public File call() throws Exception {
                HashCode buildHash = mContext.zooKeeper.getBuildHash(libNode.library, platform);
                return mContext.zooKeeper.getInstallDir(libNode.library, platform, buildHash);
            }
        };
        cmakeBuildAction.externalLibrariesCallable = new Callable<Iterable<BuildCMakeLibAction.ExternalLibrary>>() {
            @Override
            public Iterable<BuildCMakeLibAction.ExternalLibrary> call() throws Exception {
                HashSet<BuildCMakeLibAction.ExternalLibrary> retval = new HashSet<BuildCMakeLibAction.ExternalLibrary>();

                for(Library library : Iterables.concat(libNode.compileLibDependencies, libNode.testLibDependencies)){
                    HashCode buildHash = mContext.zooKeeper.getBuildHash(library, platform);
                    File installDir = mContext.zooKeeper.getInstallDir(library, platform, buildHash);

                    BuildCMakeLibAction.ExternalLibrary externLib = new BuildCMakeLibAction.ExternalLibrary();
                    externLib.library = library;
                    externLib.cmakeExportDir = new File(installDir, "cmake");
                    retval.add(externLib);
                }

                return retval;
            }
        };


        /*
        String cmakeArgs = libNode.keyValuePairs.get(KEY_CMAKE_ARGS);
        if(cmakeArgs != null) {
            for(String arg : cmakeArgs.split(" ")) {
                retval.cmakeArgs.add(arg);
                buildHashAction.mBuildParams.add("cmake:" + arg);
            }
        }
        */

        mContext.dependencyGraph.addAction(BuildCMakeLibAction.createActionName(libNode.library, platform), cmakeBuildAction);
        for(Action preBuild : preBuildDependencies) {
            mContext.dependencyGraph.addDependency(cmakeBuildAction, preBuild);
        }

        mContext.zooKeeper.mAllLibraries.add(libNode.library);

        GenerateCMakeFile generateCMakeFileAction = new GenerateCMakeFile();
        generateCMakeFileAction.mProjectRootDir = sourceDir;
        generateCMakeFileAction.mLibrary = libNode;
        generateCMakeFileAction.mOutputFile = new File(sourceDir, "CMakeLists.txt");
        mContext.dependencyGraph.addAction(GenerateCMakeFile.createActionName(libNode.library), generateCMakeFileAction);

        return null;
    }



    /*
    private Action createCheckLibraryAction(ZooKeeperParser.LibraryContext ctx, Library library, Platform platform, Action buildLibraryAction) {
        CheckBuildCacheAction checkLibAction = new CheckBuildCacheAction();
        checkLibAction.library = library;
        checkLibAction.installDir = mContext.zooKeeper.getInstallDir(library, platform);
        checkLibAction.runIfNotFound = buildLibraryAction;
        checkLibAction.dependencyGraph = mContext.dependencyGraph;

        mContext.dependencyGraph.addAction("check"+library.name, checkLibAction);

        return checkLibAction;
    }
    */

    Action getOrCreatePhonyAction(String actionName) {
        Action retval = mContext.dependencyGraph.getAction(actionName);
        if(retval == null) {
            retval = new PhonyAction();
            mContext.dependencyGraph.addAction(actionName, retval);
        }
        return retval;
    }


}
