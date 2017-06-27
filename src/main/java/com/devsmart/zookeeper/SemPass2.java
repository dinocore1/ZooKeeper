package com.devsmart.zookeeper;


import com.devsmart.zookeeper.action.BuildCMakeLibAction;
import com.devsmart.zookeeper.action.CheckLibAction;
import com.devsmart.zookeeper.action.DownloadAndUnzipAction;
import com.devsmart.zookeeper.ast.Nodes;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class SemPass2 extends ZooKeeperBaseVisitor<Void> {

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

        Action buildLibraryAction = createBuildLibraryAction(ctx, library);
        createCheckLibraryAction(ctx, library, buildLibraryAction);

        mContext.allLibraries.add(library);

        return null;
    }

    private Action createCheckLibraryAction(ZooKeeperParser.LibraryContext ctx, Library library, Action buildLibraryAction) {
        CheckLibAction checkLibAction = new CheckLibAction();
        checkLibAction.library = library;
        checkLibAction.installDir = new File(mContext.fileRoot, "install");
        checkLibAction.installDir = new File(checkLibAction.installDir, library.name);
        checkLibAction.runIfNotFound = buildLibraryAction;
        checkLibAction.dependencyGraph = mContext.dependencyGraph;

        mContext.dependencyGraph.addAction("check"+library.name, checkLibAction);

        return checkLibAction;
    }

    private static final Pattern URL_REGEX = Pattern.compile("^https?://");

    private Action createBuildLibraryAction(ZooKeeperParser.LibraryContext ctx, Library library) {
        BuildCMakeLibAction retval = null;
        Nodes.LibNode libNode = (Nodes.LibNode) mContext.nodeMap.get(ctx);

        final String KEY_SOURCE = "src";
        String srcStr = libNode.keyValuePairs.get(KEY_SOURCE);
        if(srcStr == null) {
            mContext.error("library: '" + libNode.mName + "' is missing a '" + KEY_SOURCE + "' param", ctx.getStart());
        } else {

            List<Action> preBuildDependencies = new ArrayList<Action>();
            File sourceDir;
            if(URL_REGEX.matcher(srcStr).find()) {
                String httpUrl = srcStr;
                sourceDir = new File(mContext.fileRoot, "source");
                sourceDir = new File(sourceDir, library.name);
                Action downloadAction = createHttpDownloadAction(httpUrl, sourceDir);
                mContext.dependencyGraph.addAction("download"+libNode.mName, downloadAction);
                preBuildDependencies.add(downloadAction);

            } else {
                sourceDir = new File(srcStr);
                if(!sourceDir.exists()) {
                    mContext.error("source dir does not exist: " + sourceDir.getAbsolutePath(), libNode.keyValueContext.get(KEY_SOURCE).value);
                }
            }
            retval = new BuildCMakeLibAction();
            retval.rootDir = sourceDir;
            retval.installDir = new File(mContext.fileRoot, "install");
            retval.installDir = new File(retval.installDir, library.name);

            final String CMAKE_ARGS = "cmake_args";
            String cmakeArgs = libNode.keyValuePairs.get(CMAKE_ARGS);
            if(cmakeArgs != null) {
                for(String arg : cmakeArgs.split(" ")) {
                    retval.cmakeArgs.add(arg);
                }
            }

            mContext.dependencyGraph.addAction("build"+library.name, retval);

            if(!preBuildDependencies.isEmpty()) {
                for(Action preBuild : preBuildDependencies) {
                    mContext.dependencyGraph.addDependency(retval, preBuild);
                }
            }
        }

        return retval;
    }

    Action createHttpDownloadAction(String httpUrl, File sourceDir) {
        return new DownloadAndUnzipAction(httpUrl, sourceDir);
    }
}
