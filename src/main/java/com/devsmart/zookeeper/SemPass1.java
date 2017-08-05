package com.devsmart.zookeeper;

import com.devsmart.zookeeper.ast.Nodes;
import com.devsmart.zookeeper.sourcelocation.DownloadSourceLocation;
import com.devsmart.zookeeper.sourcelocation.GitRepositorySourceLocation;
import com.devsmart.zookeeper.sourcelocation.LocalFileSystemLocation;
import com.devsmart.zookeeper.sourcelocation.LocalZipSourceLocation;
import org.antlr.v4.runtime.ParserRuleContext;

import java.io.File;
import java.util.regex.Pattern;


public class SemPass1 extends ZooKeeperBaseVisitor<Nodes.Node> {

    private static final Pattern URL_REGEX = Pattern.compile("^https?://");
    private static final Pattern ZIP_REGEX = Pattern.compile("\\.zip$");

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
        String srcStr = Nodes.escapeStringLiteral(ctx.src.getText());

        if(URL_REGEX.matcher(srcStr).find()) {
            mCurrentLibNode.src = new DownloadSourceLocation(srcStr);
        } else if(ZIP_REGEX.matcher(srcStr).find()){
            mCurrentLibNode.src = new LocalZipSourceLocation(srcStr);
        } else {
            mCurrentLibNode.src = new LocalFileSystemLocation(new File(srcStr));
        }

        return super.visitSource(ctx);
    }

    @Override
    public Nodes.Node visitGitArgs(ZooKeeperParser.GitArgsContext ctx) {
        Nodes.KeyValues gitArgs = (Nodes.KeyValues) visit(ctx.keyvalues());

        if(! (gitArgs.hasKey("url") && gitArgs.hasKey("rev")) ) {
            mContext.error("git args does not contain 'url' and 'rev'", ctx.start);
        } else {
            mCurrentLibNode.src = new GitRepositorySourceLocation(gitArgs.asMap());
        }


        return super.visitGitArgs(ctx);
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
