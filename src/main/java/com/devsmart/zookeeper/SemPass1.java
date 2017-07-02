package com.devsmart.zookeeper;

import com.devsmart.zookeeper.ast.Nodes;
import org.antlr.v4.runtime.ParserRuleContext;

import java.util.Map;


public class SemPass1 extends ZooKeeperBaseVisitor<Nodes.Node> {

    final CompilerContext mContext;
    private Nodes.LibNode mCurrentLibNode;
    private Map<String, String> mCurrentKeyValuePairs;
    private Map<String, ZooKeeperParser.KeyvalueContext> mCurrentKeyValueContext;

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
        retval.version.revision = Integer.parseInt(ctx.patch.getText());

        return putMap(ctx, retval);
    }

    @Override
    public Nodes.Node visitLibrary(ZooKeeperParser.LibraryContext ctx) {

        Nodes.VersionNode versionNode = (Nodes.VersionNode) visit(ctx.version());

        String name = ctx.name.getText();
        mCurrentLibNode = new Nodes.LibNode(name, versionNode.version);

        visit(ctx.libraryBody());

        return putMap(ctx, mCurrentLibNode);
    }

    @Override
    public Nodes.Node visitSource(ZooKeeperParser.SourceContext ctx) {
        mCurrentLibNode.src = escapeStringLiteral(ctx.src.getText());
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

    private static String escapeStringLiteral(String input) {
        input = input.substring(1, input.length()-1);
        input = input.replaceAll("\\\\\"", "\"");
        input = input.replace("\\\\", "\\");
        return input;
    }

    @Override
    public Nodes.Node visitKeyvalue(ZooKeeperParser.KeyvalueContext ctx) {
        String key = ctx.key.getText();
        String value = escapeStringLiteral(ctx.value.getText());
        mCurrentKeyValuePairs.put(key, value);
        mCurrentKeyValueContext.put(key, ctx);

        return putMap(ctx, new Nodes.KeyValue(key, value));
    }
}
