package com.devsmart.zookeeper;


import com.devsmart.zookeeper.ast.Nodes;

public class SemPass2 extends ZooKeeperBaseVisitor<Void> {

    private final CompilerContext mContext;

    public SemPass2(CompilerContext compilerContext) {
        mContext = compilerContext;
    }

    @Override
    public Void visitLibrary(ZooKeeperParser.LibraryContext ctx) {
        Nodes.LibNode libNode = (Nodes.LibNode) mContext.nodeMap.get(ctx);

        String versionStr = libNode.keyValuePairs.get("version");
        if(versionStr == null) {
            mContext.error("library: " + libNode.mName + " is missing a 'version' param", ctx.getStart());
        }

        return null;
    }
}
