package com.devsmart.zookeeper;

import com.devsmart.zookeeper.ast.Nodes;
import org.antlr.v4.runtime.ParserRuleContext;

import java.util.Map;


public class SemPass1 extends ZooKeeperBaseVisitor<Nodes.Node> {

    final CompilerContext mContext;
    private Nodes.LibNode mCurrentLibNode;
    private Map<String, String> mCurrentKeyValuePairs;

    public SemPass1(CompilerContext context) {
        mContext = context;
    }

    private Nodes.Node putMap(ParserRuleContext ctx, Nodes.Node node) {
        mContext.nodeMap.put(ctx, node);
        return node;
    }

    @Override
    public Nodes.Node visitLibrary(ZooKeeperParser.LibraryContext ctx) {
        String name = ctx.name.getText();
        mCurrentLibNode = new Nodes.LibNode(name);
        mCurrentKeyValuePairs = mCurrentLibNode.keyValuePairs;
        visit(ctx.keyvalues());

        return putMap(ctx, mCurrentLibNode);
    }

    @Override
    public Nodes.Node visitKeyvalue(ZooKeeperParser.KeyvalueContext ctx) {
        String key = ctx.key.getText();
        String value = ctx.value.getText();
        mCurrentKeyValuePairs.put(key, value);

        return putMap(ctx, new Nodes.KeyValue(key, value));
    }
}
