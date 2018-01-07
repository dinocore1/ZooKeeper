package com.devsmart.zookeeper;

import com.devsmart.zookeeper.ast.Nodes;
import org.antlr.v4.runtime.ParserRuleContext;


public class SemPass1 extends ZooKeeperBaseVisitor<Nodes.Node> {

    private final CompilerContext mContext;
    private Nodes.LibraryDefNode mCurrentLibDef;

    public SemPass1(CompilerContext context) {
        mContext = context;
    }

    private Nodes.Node putMap(ParserRuleContext ctx, Nodes.Node node) {
        mContext.nodeMap.put(ctx, node);
        return node;
    }

    @Override
    public Nodes.Node visitVersion(ZooKeeperParser.VersionContext ctx) {
        Nodes.VersionNode retval = new Nodes.VersionNode();
        retval.version.major = Integer.parseInt(ctx.major.getText());
        retval.version.minor = Integer.parseInt(ctx.minor.getText());
        retval.version.patch = Integer.parseInt(ctx.patch.getText());

        return putMap(ctx, retval);
    }

    @Override
    public Nodes.Node visitLibraryMetadataDef(ZooKeeperParser.LibraryMetadataDefContext ctx) {
        mCurrentLibDef = new Nodes.LibraryDefNode();
        mCurrentLibDef.libName = ctx.name.getText();
        mCurrentLibDef.versionNode = (Nodes.VersionNode) visit(ctx.version());
        visit(ctx.object());

        return putMap(ctx, mCurrentLibDef);
    }

    @Override
    public Nodes.Node visitObject(ZooKeeperParser.ObjectContext ctx) {

        return null;

    }
}
