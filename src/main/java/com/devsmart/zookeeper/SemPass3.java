package com.devsmart.zookeeper;


import com.devsmart.zookeeper.action.*;
import com.devsmart.zookeeper.ast.Nodes;
import com.google.common.collect.Iterables;

public class SemPass3 extends ZooKeeperBaseVisitor<Void> {

    private final CompilerContext mContext;

    public SemPass3(CompilerContext context) {
        mContext = context;
    }

    @Override
    public Void visitLibrary(ZooKeeperParser.LibraryContext ctx) {
        final Platform platform = mContext.zooKeeper.getBuildPlatform();
        final Nodes.LibNode libNode = (Nodes.LibNode) mContext.nodeMap.get(ctx);
        final LibraryPlatformKey key = new LibraryPlatformKey(libNode.library, platform);

        Action libConfigAction = mContext.dependencyGraph.getAction(CMakeConfigAction.createActionName(key));

        for(Library library : Iterables.concat(libNode.compileLibDependencies, libNode.testLibDependencies)) {
            Action secureDependencyAction = mContext.dependencyGraph.getAction(VerifyLibraryInstalledAction.createActionName(library, platform));
            if(secureDependencyAction != null) {
                mContext.dependencyGraph.addDependency(libConfigAction, secureDependencyAction);
            } else {
                mContext.error("could not find build definition for: " + library, ctx.libraryBody().dependencies().getStart());
            }
        }

        return null;
    }
}