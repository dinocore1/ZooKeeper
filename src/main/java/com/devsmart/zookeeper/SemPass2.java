package com.devsmart.zookeeper;


import com.devsmart.StringUtils;
import com.devsmart.zookeeper.action.*;
import com.devsmart.zookeeper.ast.Nodes;
import com.google.common.collect.ImmutableList;

import java.io.File;
import java.util.ArrayList;
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
    private CheckBuildCacheAction mCurrentCheckLibCacheAction;

    public SemPass2(CompilerContext compilerContext) {
        mContext = compilerContext;
    }

    @Override
    public Void visitLibrary(ZooKeeperParser.LibraryContext ctx) {
        Nodes.LibNode libNode = (Nodes.LibNode) mContext.nodeMap.get(ctx);

        Platform platform = ZooKeeper.getNativePlatform();
        createBuildLibraryAction(ctx, platform);
        /*

        Library library = new Library(libNode.name, version);

        Platform platform;
        final String KEY_PLATFORM = "platform";
        String platformStr = libNode.keyValuePairs.get(KEY_PLATFORM);
        if(platformStr != null) {
            platform = Platform.parse(platformStr);
        } else {
            platform = ZooKeeper.getNativePlatform();
        }

        Action buildLibraryAction = createBuildLibraryAction(ctx, library, platform);
        //createCheckLibraryAction(ctx, library, platform, buildLibraryAction);

        mContext.allLibraries.add(library);

        */

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



    private void createBuildLibraryAction(ZooKeeperParser.LibraryContext ctx, final Platform platform) {
        final Nodes.LibNode libNode = (Nodes.LibNode) mContext.nodeMap.get(ctx);
        List<Action> preBuildDependencies = new ArrayList<Action>();

        final ComputeBuildHash buildHashAction = new ComputeBuildHash();
        mContext.dependencyGraph.addAction(ComputeBuildHash.createActionName(libNode.library, platform), buildHashAction);
        preBuildDependencies.add(buildHashAction);

        mCurrentCheckLibCacheAction = new CheckBuildCacheAction();
        mContext.dependencyGraph.addAction(CheckBuildCacheAction.createActionName(libNode.library, platform), mCurrentCheckLibCacheAction);
        mCurrentCheckLibCacheAction.libraryHash = buildHashAction.libraryHash;


        File sourceDir;
        if(StringUtils.isEmptyString(libNode.src)){
            sourceDir = mContext.fileRoot;
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

        BuildCMakeLibAction cmakeBuildAction = new BuildCMakeLibAction();
        cmakeBuildAction.rootDir = sourceDir;
        cmakeBuildAction.installDirCallable = new Callable<File>() {
            @Override
            public File call() throws Exception {
                return mContext.zooKeeper.getInstallDir(libNode.library, platform, buildHashAction.libraryHash.get());
            }
        };

        buildHashAction.mSourceDir = sourceDir;
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

        Action rootAction = getOrCreatePhonyAction(BuildCMakeLibAction.createActionName(libNode.library));
        mContext.dependencyGraph.addDependency(rootAction, cmakeBuildAction);

        if(!preBuildDependencies.isEmpty()) {
            for(Action preBuild : preBuildDependencies) {
                mContext.dependencyGraph.addDependency(cmakeBuildAction, preBuild);
            }
        }
    }

    Action getOrCreatePhonyAction(String actionName) {
        Action retval = mContext.dependencyGraph.getAction(actionName);
        if(retval == null) {
            retval = new PhonyAction();
            mContext.dependencyGraph.addAction(actionName, retval);
        }
        return retval;
    }


}
