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

        return null;
    }
}
