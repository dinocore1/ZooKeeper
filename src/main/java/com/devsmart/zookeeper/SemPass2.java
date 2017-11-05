package com.devsmart.zookeeper;


import com.devsmart.zookeeper.action.*;
import com.devsmart.zookeeper.ast.Nodes;
import com.devsmart.zookeeper.sourcelocation.DownloadSourceLocation;
import com.devsmart.zookeeper.sourcelocation.GitRepositorySourceLocation;
import com.devsmart.zookeeper.sourcelocation.LocalFileSystemLocation;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.hash.HashCode;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

public class SemPass2 extends ZooKeeperBaseVisitor<Void> {

    static final String KEY_CMAKE_ARGS = "cmake_args";

    static final ImmutableList<String> BUILD_ARGS;

    static {
        BUILD_ARGS = ImmutableList.of(KEY_CMAKE_ARGS);
    }

    private final CompilerContext mContext;
    private ArrayList<Nodes.LibNode> mLibraries = new ArrayList<Nodes.LibNode>();
    private ArrayList<Nodes.PlatformNode> mPlatforms = new ArrayList<Nodes.PlatformNode>();
    private HashMap<LibraryPlatformKey, CMakeBuildContext> mBuildContextMap = new HashMap<LibraryPlatformKey, CMakeBuildContext>();

    public SemPass2(CompilerContext compilerContext) {
        mContext = compilerContext;

        Nodes.PlatformNode nativePlatform = new Nodes.PlatformNode(null);
        nativePlatform.platform = mContext.zooKeeper.getNativeBuildPlatform();
        nativePlatform.keyValues = null;

        mPlatforms.add(nativePlatform);
    }

    private class ActionBuilder {

        private final Action mAction;

        ActionBuilder(Action a) {
            mAction = a;
            mContext.dependencyGraph.addAction(mAction);
        }

        ActionBuilder dependsOn(Action other) {
            mContext.dependencyGraph.addDependency(mAction, other);
            return this;
        }

        Action get() {
            return mAction;
        }

    }

    @Override
    public Void visitFile(ZooKeeperParser.FileContext ctx) {
        super.visitChildren(ctx);

        for(Nodes.PlatformNode platformNode : mPlatforms) {
            for(Nodes.LibNode libNode : mLibraries) {
                createLibraryPlatformCombo(libNode, platformNode);
            }

            for(Nodes.LibNode libNode : mLibraries) {
                resolveBuildParams(libNode, platformNode);
                addTransientLibraryDependencies(libNode, platformNode);
            }
        }

        return null;
    }

    private void resolveBuildParams(Nodes.LibNode libNode, Nodes.PlatformNode platformNode) {
        final Platform  platform = platformNode.platform;
        final LibraryPlatformKey libraryPlatformKey = new LibraryPlatformKey(libNode.library, platform);
        final CMakeBuildContext buildContext = mBuildContextMap.get(libraryPlatformKey);

        ComputeBuildHash buildHashAction = (ComputeBuildHash) mContext.dependencyGraph.getAction(Utils.createActionName("hash", libraryPlatformKey.toString()));

        if(libNode.cmakeArgs != null) {
            for(Nodes.KeyValue keyvalue : libNode.cmakeArgs.keyValues){
                String arg = keyvalue.getKey()+"="+mContext.VM.interpretString(keyvalue.getValue());
                buildContext.cMakeArgs.add(arg);
                buildHashAction.mBuildParams.add("cmake:"+arg);
            }
        }

        if(platformNode.keyValues != null) {
            for (Nodes.KeyValue keyvalue : platformNode.keyValues.keyValues) {
                String arg = keyvalue.getKey() + "=" + mContext.VM.interpretString(keyvalue.getValue());
                buildContext.cMakeArgs.add(arg);
                buildHashAction.mBuildParams.add("cmake:" + arg);
            }
        }

    }

    private void createLibraryPlatformCombo(final Nodes.LibNode libNode, final Nodes.PlatformNode platformNode) {
        final Platform  platform = platformNode.platform;
        final LibraryPlatformKey libraryPlatformKey = new LibraryPlatformKey(libNode.library, platform);
        final CMakeBuildContext buildContext = new CMakeBuildContext(mContext.zooKeeper, libNode.library, platform);
        mBuildContextMap.put(libraryPlatformKey, buildContext);

        File buildDir = new File(mContext.fileRoot, "builds");
        buildDir = new File(buildDir, libraryPlatformKey.lib.name);
        buildDir = new File(buildDir, libraryPlatformKey.platform.toString());

        buildContext.buildDir.set(buildDir);

        ////////// Compute Build Hash //////////////
        final ComputeBuildHash buildHashAction = new ComputeBuildHash() {
            @Override
            public void doIt() throws Exception {
                super.doIt();
                final HashCode buildHash = buildContext.buildHash.get();
                mContext.zooKeeper.mLibraryHashTable.put(buildContext.getPlatformKey(), buildHash);
                buildContext.installDir.set(mContext.zooKeeper.getInstallDir(libraryPlatformKey.lib, libraryPlatformKey.platform, buildHash));
            }
        };
        buildHashAction.libraryHash = buildContext.buildHash;
        buildHashAction.mSourceDir = buildContext.sourceDir;
        mContext.dependencyGraph.addAction(Utils.createActionName("hash", libraryPlatformKey.toString()), buildHashAction);


        ///////// Configure Action /////////////
        CMakeConfigAction cmakeConfigAction = new CMakeConfigAction(buildContext) {
            @Override
            public void doIt() throws Exception {
                for(Library library : Iterables.concat(libNode.compileLibDependencies, libNode.testLibDependencies)){
                    HashCode buildHash = mContext.zooKeeper.getBuildHash(library, platform);
                    File installDir = mContext.zooKeeper.getInstallDir(library, platform, buildHash);
                    buildContext.mExternalLibDependencies.add(
                            new CMakeBuildContext.ExternalLibrary(library, new File(installDir, "cmake")));
                }

                super.doIt();
            }
        };

        if(Platform.WIN64.equals(libraryPlatformKey.platform)) {
            cmakeConfigAction.mGenerator = "Visual Studio 14 2015 Win64";
        }
        mContext.dependencyGraph.addAction(CMakeConfigAction.createActionName(libraryPlatformKey), cmakeConfigAction);
        mContext.dependencyGraph.addDependency(cmakeConfigAction, buildHashAction);


        File sourceDir = null;
        if(libNode.src == null){
            sourceDir = new File("").getAbsoluteFile();
            buildContext.sourceDir.set(sourceDir);
        } else if(libNode.src instanceof DownloadSourceLocation){
            String httpUrl = ((DownloadSourceLocation) libNode.src).mUrl;
            DownloadAndUnzipAction downloadAction = new DownloadAndUnzipAction(httpUrl, buildContext.sourceDir, mContext.zooKeeper);
            mContext.dependencyGraph.addAction(Utils.createActionName("download", libNode.library.name), downloadAction);
            mContext.dependencyGraph.addDependency(buildHashAction, downloadAction);
            mContext.dependencyGraph.addDependency(cmakeConfigAction, downloadAction);
        } else if(libNode.src instanceof LocalFileSystemLocation){
            sourceDir = ((LocalFileSystemLocation) libNode.src).mSourceFolder;
            if(!sourceDir.exists()) {
                mContext.error("source dir does not exist: " + sourceDir.getAbsolutePath(), null);
            }
            buildContext.sourceDir.set(sourceDir);
        } else if(libNode.src instanceof GitRepositorySourceLocation) {
            ConfigLocalGitRepoAction localGitRepoAction = new ConfigLocalGitRepoAction(libNode.library, (GitRepositorySourceLocation)libNode.src, buildContext.sourceDir, mContext.zooKeeper);
            mContext.dependencyGraph.addAction(Utils.createActionName("clone", libNode.library.name), localGitRepoAction);
            mContext.dependencyGraph.addDependency(buildHashAction, localGitRepoAction);
            mContext.dependencyGraph.addDependency(cmakeConfigAction, localGitRepoAction);

        }

        /////// Build Action //////////
        CMakeBuildAction cmakeBuildAction = new CMakeBuildAction(buildContext);
        mContext.dependencyGraph.addAction(Utils.createActionName("build", libraryPlatformKey.toString()), cmakeBuildAction);
        mContext.dependencyGraph.addDependency(cmakeBuildAction, cmakeConfigAction);


        /////// Install Action /////
        CMakeInstallAction cmakeInstallAction = new CMakeInstallAction(buildContext);
        mContext.dependencyGraph.addAction(CMakeInstallAction.createActionName(libraryPlatformKey), cmakeInstallAction);
        mContext.dependencyGraph.addDependency(cmakeInstallAction, cmakeConfigAction);
        mContext.dependencyGraph.addDependency(cmakeInstallAction, cmakeBuildAction);


        /////// Archive Action ////
        ArchiveAction archiveAction = new ArchiveAction(buildContext);
        mContext.dependencyGraph.addAction(ArchiveAction.createActionName(libraryPlatformKey), archiveAction);
        mContext.dependencyGraph.addDependency(archiveAction, cmakeInstallAction);

        ////// Verify Installed Action ////
        final VerifyLibraryInstalledAction verifyLibraryInstalledAction = new VerifyLibraryInstalledAction(libNode.library, platform, mContext.zooKeeper);
        mContext.dependencyGraph.addAction(VerifyLibraryInstalledAction.createActionName(libNode.library, platform), verifyLibraryInstalledAction);
        mContext.dependencyGraph.addDependency(verifyLibraryInstalledAction, buildHashAction);
        verifyLibraryInstalledAction.runIfNotInstalled = cmakeInstallAction;


        mContext.zooKeeper.mAllLibraries.add(libNode.library);


        ///// Gen CMake File ////
        GenerateCMakeFile generateCMakeFileAction = new GenerateCMakeFile();
        generateCMakeFileAction.mProjectRootDir = sourceDir;
        generateCMakeFileAction.mLibrary = libNode;
        generateCMakeFileAction.mOutputFile = new File(sourceDir, "CMakeLists.txt");
        mContext.dependencyGraph.addAction(Utils.createActionName("genCMake", libraryPlatformKey.lib.name), generateCMakeFileAction);
    }


    private void addTransientLibraryDependencies(final Nodes.LibNode libNode, final Nodes.PlatformNode platformNode) {
        final Platform platform = platformNode.platform;
        final LibraryPlatformKey key = new LibraryPlatformKey(libNode.library, platform);

        Action libConfigAction = mContext.dependencyGraph.getAction(CMakeConfigAction.createActionName(key));

        for(Library library : Iterables.concat(libNode.compileLibDependencies, libNode.testLibDependencies)) {
            Action secureDependencyAction = mContext.dependencyGraph.getAction(VerifyLibraryInstalledAction.createActionName(library, platform));
            if(secureDependencyAction != null) {
                mContext.dependencyGraph.addDependency(libConfigAction, secureDependencyAction);
            } else {
                mContext.error("could not find build definition for: " + library, libNode.mCtx.libraryBody().dependencies().getStart());
            }
        }
    }

    @Override
    public Void visitPlatform(ZooKeeperParser.PlatformContext ctx) {
        final Nodes.PlatformNode platformNode = (Nodes.PlatformNode) mContext.nodeMap.get(ctx);
        mPlatforms.add(platformNode);

        return null;
    }

    @Override
    public Void visitLibrary(ZooKeeperParser.LibraryContext ctx) {
        final Nodes.LibNode libNode = (Nodes.LibNode) mContext.nodeMap.get(ctx);
        mLibraries.add(libNode);

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
