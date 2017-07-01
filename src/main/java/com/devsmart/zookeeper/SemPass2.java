package com.devsmart.zookeeper;


import com.devsmart.zookeeper.action.*;
import com.devsmart.zookeeper.ast.Nodes;
import com.google.common.collect.ImmutableList;

import java.io.File;
import java.nio.file.Files;
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

    public SemPass2(CompilerContext compilerContext) {
        mContext = compilerContext;
    }

    @Override
    public Void visitLibrary(ZooKeeperParser.LibraryContext ctx) {
        Nodes.LibNode libNode = (Nodes.LibNode) mContext.nodeMap.get(ctx);

        final String KEY_VERSION = "version";

        Version version = null;
        String versionStr = libNode.keyValuePairs.get(KEY_VERSION);
        if(versionStr == null) {
            mContext.error("library: '" + libNode.mName + "' is missing a '"+KEY_VERSION+"' param", ctx.getStart());
        } else {
            version = Version.fromString(versionStr);
            if(version == null) {
                mContext.error("could not parse version string: " + versionStr, libNode.keyValueContext.get(KEY_VERSION).value);
            }
        }

        Library library = new Library(libNode.mName, version);

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

        return null;
    }



    /*
    private Action createCheckLibraryAction(ZooKeeperParser.LibraryContext ctx, Library library, Platform platform, Action buildLibraryAction) {
        CheckLibAction checkLibAction = new CheckLibAction();
        checkLibAction.library = library;
        checkLibAction.installDir = mContext.zooKeeper.getInstallDir(library, platform);
        checkLibAction.runIfNotFound = buildLibraryAction;
        checkLibAction.dependencyGraph = mContext.dependencyGraph;

        mContext.dependencyGraph.addAction("check"+library.name, checkLibAction);

        return checkLibAction;
    }
    */



    private Action createBuildLibraryAction(ZooKeeperParser.LibraryContext ctx, final Library library, final Platform platform) {

        BuildCMakeLibAction retval = null;
        Nodes.LibNode libNode = (Nodes.LibNode) mContext.nodeMap.get(ctx);

        final String KEY_SOURCE = "src";
        String srcStr = libNode.keyValuePairs.get(KEY_SOURCE);
        if(srcStr == null) {
            mContext.error("library: '" + libNode.mName + "' is missing a '" + KEY_SOURCE + "' param", ctx.getStart());
        } else {
            final ComputeBuildHash buildHashAction = new ComputeBuildHash();
            mContext.dependencyGraph.addAction(ComputeBuildHash.createActionName(library, platform), buildHashAction);

            List<Action> preBuildDependencies = new ArrayList<Action>();
            preBuildDependencies.add(buildHashAction);

            File sourceDir;
            if(URL_REGEX.matcher(srcStr).find()) {
                String httpUrl = srcStr;
                sourceDir = new File(mContext.fileRoot, "source");
                sourceDir = new File(sourceDir, library.name);
                Action downloadAction = createHttpDownloadAction(httpUrl, sourceDir);
                mContext.dependencyGraph.addAction("download"+libNode.mName, downloadAction);
                preBuildDependencies.add(downloadAction);
                mContext.dependencyGraph.addDependency(buildHashAction, downloadAction);

            } else {
                sourceDir = new File(srcStr);
                if(!sourceDir.exists()) {
                    mContext.error("source dir does not exist: " + sourceDir.getAbsolutePath(), libNode.keyValueContext.get(KEY_SOURCE).value);
                }
            }
            retval = new BuildCMakeLibAction();
            retval.rootDir = sourceDir;
            retval.installDirCallable = new Callable<File>() {
                @Override
                public File call() throws Exception {
                    return mContext.zooKeeper.getInstallDir(library, platform, buildHashAction.libraryHash.get());
                }
            };

            buildHashAction.mSourceDir = sourceDir;
            String cmakeArgs = libNode.keyValuePairs.get(KEY_CMAKE_ARGS);
            if(cmakeArgs != null) {
                for(String arg : cmakeArgs.split(" ")) {
                    retval.cmakeArgs.add(arg);
                    buildHashAction.mBuildParams.add("cmake:" + arg);
                }
            }

            mContext.dependencyGraph.addAction(BuildCMakeLibAction.createActionName(library, platform), retval);

            Action rootAction = getOrCreatePhonyAction(BuildCMakeLibAction.createActionName(library));
            mContext.dependencyGraph.addDependency(rootAction, retval);

            if(!preBuildDependencies.isEmpty()) {
                for(Action preBuild : preBuildDependencies) {
                    mContext.dependencyGraph.addDependency(retval, preBuild);
                }
            }
        }

        return retval;
    }

    Action getOrCreatePhonyAction(String actionName) {
        Action retval = mContext.dependencyGraph.getAction(actionName);
        if(retval == null) {
            retval = new PhonyAction();
            mContext.dependencyGraph.addAction(actionName, retval);
        }
        return retval;
    }

    Action createHttpDownloadAction(String httpUrl, File sourceDir) {
        return new DownloadAndUnzipAction(httpUrl, sourceDir);
    }
}
