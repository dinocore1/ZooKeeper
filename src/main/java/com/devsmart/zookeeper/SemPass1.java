package com.devsmart.zookeeper;

import com.devsmart.zookeeper.ast.Nodes;
import org.antlr.v4.runtime.ParserRuleContext;

import java.util.Map;


public class SemPass1 extends ZooKeeperBaseVisitor<Nodes.Node> {

    final CompilerContext mContext;
    private Nodes.LibNode mCurrentLibNode;

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
    public Nodes.Node visitLibrary(ZooKeeperParser.LibraryContext ctx) {

        mCurrentLibNode = new Nodes.LibNode(ctx);
        Nodes.VersionNode versionNode = (Nodes.VersionNode) visit(ctx.version());
        mCurrentLibNode.library = new Library(ctx.name.getText(), versionNode.version);

        visit(ctx.libraryBody());

        return putMap(ctx, mCurrentLibNode);
    }

    @Override
    public Nodes.Node visitSource(ZooKeeperParser.SourceContext ctx) {
        mCurrentLibNode.src = Nodes.escapeStringLiteral(ctx.src.getText());
        return super.visitSource(ctx);
    }

    @Override
    public Nodes.Node visitDependList(ZooKeeperParser.DependListContext ctx) {
        if(ctx.COMPILE() != null) {
            String libName = ctx.ID().getText();
            Nodes.VersionNode versionNode = (Nodes.VersionNode) visit(ctx.version());
            Library library = new Library(libName, versionNode.version);
            mCurrentLibNode.compileLibDependencies.add(library);
        } else if(ctx.TEST() != null) {
            String libName = ctx.ID().getText();
            Nodes.VersionNode versionNode = (Nodes.VersionNode) visit(ctx.version());
            Library library = new Library(libName, versionNode.version);
            mCurrentLibNode.testLibDependencies.add(library);
        }

        return super.visitDependList(ctx);
    }

    @Override
    public Nodes.Node visitCmakeArgs(ZooKeeperParser.CmakeArgsContext ctx) {
        mCurrentLibNode.cmakeArgs = (Nodes.KeyValues) visit(ctx.keyvalues());

        return super.visitCmakeArgs(ctx);
    }

    @Override
    public Nodes.Node visitKeyvalues(ZooKeeperParser.KeyvaluesContext ctx) {
        Nodes.KeyValues retval = new Nodes.KeyValues(ctx);
        for(ZooKeeperParser.KeyvalueContext keyvalueCtx : ctx.keyvalue()) {
            retval.keyValues.add((Nodes.KeyValue) visit(keyvalueCtx));
        }

        return putMap(ctx, retval);
    }

    @Override
    public Nodes.Node visitKeyvalue(ZooKeeperParser.KeyvalueContext ctx) {
        return putMap(ctx, new Nodes.KeyValue(ctx));
    }
}
