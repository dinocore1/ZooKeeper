package com.devsmart.zookeeper;

import com.devsmart.zookeeper.ast.Nodes;
import org.antlr.v4.runtime.ParserRuleContext;

import java.util.ArrayList;


public class SemPass1 extends ZooKeeperBaseVisitor<Nodes.Node> {

    private final CompilerContext mContext;

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
    public Nodes.Node visitLibraryBuildDef(ZooKeeperParser.LibraryBuildDefContext ctx) {
        Nodes.BuildLibraryDefNode retval = new Nodes.BuildLibraryDefNode();
        retval.libName = ctx.name.getText();
        retval.versionNode = (Nodes.VersionNode) visit(ctx.version());
        retval.objectNode = (Nodes.ObjectNode) visit(ctx.object());

        return putMap(ctx, retval);
    }

    @Override
    public Nodes.Node visitExeBuildDef(ZooKeeperParser.ExeBuildDefContext ctx) {
        Nodes.BuildExeDefNode retval = new Nodes.BuildExeDefNode();
        retval.exeName = ctx.name.getText();
        retval.versionNode = (Nodes.VersionNode) visit(ctx.version());
        retval.objectNode = (Nodes.ObjectNode) visit(ctx.object());

        return putMap(ctx, retval);
    }

    @Override
    public Nodes.Node visitPrecompiledLibDef(ZooKeeperParser.PrecompiledLibDefContext ctx) {
        Nodes.PrecompiledLibraryDefNode retval = new Nodes.PrecompiledLibraryDefNode();
        retval.libName = ctx.name.getText();
        retval.versionNode = (Nodes.VersionNode) visit(ctx.version());
        retval.objectNode = (Nodes.ObjectNode) visit(ctx.object());

        return putMap(ctx, retval);
    }

    @Override
    public Nodes.Node visitString(ZooKeeperParser.StringContext ctx) {
        String value = ctx.STRING_LITERAL().getText();
        Nodes.StringNode stringNode = new Nodes.StringNode(Nodes.escapeStringLiteral(value));

        return putMap(ctx, stringNode);
    }

    @Override
    public Nodes.Node visitArray(ZooKeeperParser.ArrayContext ctx) {
        ArrayList<Nodes.ValueNode> values = new ArrayList<Nodes.ValueNode>();
        for(ZooKeeperParser.ValueContext valueCtx : ctx.value()) {
            values.add((Nodes.ValueNode) visit(valueCtx));

        }

        return putMap(ctx, new Nodes.ArrayNode(values));
    }


    @Override
    public Nodes.Node visitObject(ZooKeeperParser.ObjectContext ctx) {
        Nodes.KeyValueEntriesNode entries = (Nodes.KeyValueEntriesNode) visit(ctx.keyValueEntries());
        return putMap(ctx, new Nodes.ObjectNode(entries));

    }

    @Override
    public Nodes.Node visitKeyValueEntries(ZooKeeperParser.KeyValueEntriesContext ctx) {

        if(ctx.key != null) {
            Nodes.KeyValueEntriesNode entries = (Nodes.KeyValueEntriesNode) visit(ctx.keyValueEntries());
            String key = ctx.key.getText();
            Nodes.ValueNode value = (Nodes.ValueNode) visit(ctx.value());
            entries.add(key, value);
            return putMap(ctx, entries);
        } else {
            return putMap(ctx, new Nodes.KeyValueEntriesNode());
        }


    }
}
