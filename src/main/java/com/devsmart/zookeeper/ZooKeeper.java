package com.devsmart.zookeeper;


import com.devsmart.zookeeper.action.ListAllActionsAction;
import com.devsmart.zookeeper.action.PhonyAction;
import com.devsmart.zookeeper.action.VerifyLibraryInstalledAction;
import com.devsmart.zookeeper.ast.Nodes;
import com.google.common.collect.ComparisonChain;
import com.google.common.hash.HashCode;
import com.google.common.io.BaseEncoding;
import com.google.common.primitives.SignedBytes;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.apache.commons.cli.*;
import org.jetbrains.annotations.NotNull;
import org.mapdb.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.TreeMap;

public class ZooKeeper {

    public static final String VAR_CMAKE_EXE = "CMAKE_EXE";

    public static class DownloadCache implements Comparable<DownloadCache> {
        public Date downloadTime;
        public HashCode sourceHash;

        @Override
        public int compareTo(@NotNull DownloadCache o) {
            return ComparisonChain
                    .start()
                    .compare(downloadTime, o.downloadTime)
                    .compare(sourceHash.asBytes(), o.sourceHash.asBytes(), SignedBytes.lexicographicalComparator())
                    .result();

        }
    }

    public static class DownloadCacheSerializer implements Serializer<DownloadCache> {

        @Override
        public void serialize(@NotNull DataOutput2 out, @NotNull DownloadCache value) throws IOException {
            out.packLong(value.downloadTime.getTime());

            byte[] hashBytes = value.sourceHash.asBytes();
            out.packInt(hashBytes.length);
            out.write(hashBytes);
        }

        @Override
        public DownloadCache deserialize(@NotNull DataInput2 input, int available) throws IOException {
            DownloadCache retval = new DownloadCache();
            retval.downloadTime = new Date(input.unpackLong());

            byte[] hashBytes = new byte[input.unpackInt()];
            input.readFully(hashBytes);
            retval.sourceHash = HashCode.fromBytes(hashBytes);
            return retval;
        }

        @Override
        public int compare(DownloadCache first, DownloadCache second) {
            return first.compareTo(second);
        }
    }

    public final DB mDB;
    public final VM mVM = new VM();
    public final HTreeMap<String, DownloadCache> mDownloadCache;
    public DependencyGraph mDependencyGraph = new DependencyGraph();
    public File mZooKeeperRoot;
    public ArrayList<Library> mAllLibraries = new ArrayList<Library>();
    public TreeMap<LibraryPlatformKey, HashCode> mLibraryHashTable = new TreeMap<LibraryPlatformKey, HashCode>();

    ZooKeeper() {
        mZooKeeperRoot = new File(System.getProperty("user.home"));
        mZooKeeperRoot = new File(mZooKeeperRoot, ".zookeeper");
        mZooKeeperRoot.mkdirs();

        File dbFile = new File(mZooKeeperRoot, "zookeeper.db");

        mDB = DBMaker.fileDB(dbFile)
                .closeOnJvmShutdown()
                .checksumHeaderBypass()
                .make();

        mDownloadCache = mDB.hashMap("downloadCached", Serializer.STRING, new DownloadCacheSerializer())
                .createOrOpen();

        mVM.setVar(VAR_CMAKE_EXE, "cmake");

        mVM.setVar(System.getenv());
        mVM.setVar("PROJECT_DIR", new File("").getAbsolutePath());
    }

    public Platform getNativeBuildPlatform() {
        return getNativePlatform();
    }

    public HashCode getBuildHash(Library library, Platform platform) {
        LibraryPlatformKey key = new LibraryPlatformKey(library, platform);
        return mLibraryHashTable.get(key);
    }

    public void setBuildHash(Library library, Platform platform, HashCode hash) {
        LibraryPlatformKey key = new LibraryPlatformKey(library, platform);
        mLibraryHashTable.put(key, hash);
    }

    public File getInstallDir(Library library, Platform platform, HashCode buildHash) {
        File retval = new File(mZooKeeperRoot, "install");
        retval = new File(retval, library.name);
        retval = new File(retval, platform.toString());
        retval = new File(retval, BaseEncoding.base16().encode(buildHash.asBytes()).substring(0, 7));
        return retval;
    }

    public static Platform getNativePlatform() {
        Platform.OS os;
        String OSString = System.getProperty("os.name", "generic").toLowerCase(Locale.ENGLISH);
        if (OSString.contains("mac") || OSString.contains("darwin")) {
            os = Platform.OS.osx;
        } else if (OSString.contains("win")) {
            os = Platform.OS.win;
        } else if (OSString.contains("nux")) {
            os = Platform.OS.linux;
        } else {
            os = Platform.OS.UNKNOWN;
        }

        Platform.ARCH arch;
        String archStr = System.getProperty("os.arch", "generic").toLowerCase(Locale.ENGLISH);
        if(archStr.contains("x86_64") || archStr.contains("amd64")) {
            arch = Platform.ARCH.x86_64;
        } else if(archStr.contains("x86")){
            arch = Platform.ARCH.x86;
        } else {
            arch = Platform.ARCH.UNKNOWN;
        }

        return new Platform(os, arch);
    }

    public boolean compileInputStream(ANTLRInputStream inputStream) {
        CompilerContext compilerContext = new CompilerContext();
        compilerContext.dependencyGraph = mDependencyGraph;
        compilerContext.fileRoot = mZooKeeperRoot;
        compilerContext.VM = mVM;
        compilerContext.zooKeeper = this;

        ZooKeeperLexer lexer = new ZooKeeperLexer(inputStream);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        ZooKeeperParser parser = new ZooKeeperParser(tokens);
        parser.addErrorListener(compilerContext.parserErrorHandler);

        ZooKeeperParser.FileContext root = parser.file();

        SemPass1 semPass1 = new SemPass1(compilerContext);
        Nodes.Node fileNode = semPass1.visitFile(root);

        if(compilerContext.hasErrors()) {
            compilerContext.reportMessages(System.err);
            return false;
        }

        SemPass2 semPass2 = new SemPass2(compilerContext);
        semPass2.visit(root);

        if(compilerContext.hasErrors()) {
            compilerContext.reportMessages(System.err);
            return false;
        }

        Platform buildPlatform = getNativeBuildPlatform();
        PhonyAction checkAllLibsAction = new PhonyAction();
        mDependencyGraph.addAction("all", checkAllLibsAction);
        for(Library lib : mAllLibraries) {
            Action checkLib = mDependencyGraph.getAction(VerifyLibraryInstalledAction.createActionName(lib, buildPlatform));
            mDependencyGraph.addDependency(checkAllLibsAction, checkLib);
        }

        mDependencyGraph.addAction("listActions", new ListAllActionsAction(this));

        return true;
    }

    public boolean compileFile(File file) throws IOException {
        FileInputStream fin = new FileInputStream(file);
        ANTLRInputStream inputStream = new ANTLRInputStream(fin);
        inputStream.name = file.getAbsolutePath();
        return compileInputStream(inputStream);
    }

    public boolean compile(InputStream in) throws IOException {
        ANTLRInputStream inputStream = new ANTLRInputStream(in);
        return compileInputStream(inputStream);
    }

    public static void main(String[] args) {

        ZooKeeper zoo = new ZooKeeper();

        Options options = new Options();

        options.addOption(Option.builder("i")
                .hasArg()
                .argName("input .zoo file")
                .desc("the input ZOO file")
                .build());

        CommandLineParser parser = new DefaultParser();
        try {
            CommandLine cmdline = parser.parse(options, args);

            String inputFileStr = cmdline.getOptionValue("i", "build.zoo");
            File inputFile = new File(inputFileStr);
            if(inputFile.exists() && inputFile.isFile()) {
                zoo.compileFile(inputFile);
            } else {
                System.err.println("could not open file: " + inputFile.getAbsolutePath());
            }

            String[] unparsedArgs = cmdline.getArgs();
            if(unparsedArgs == null || unparsedArgs.length == 0) {
                System.out.println("try running 'listActions' to see available actions");
                System.exit(1);
            }
            for(String target : unparsedArgs) {
                Action action = zoo.mDependencyGraph.getAction(target);
                if(action != null) {
                    zoo.mDependencyGraph.runAction(action);
                } else {
                    System.err.println("no target with name: " + target);
                }
            }



        } catch (ParseException e) {
            System.err.println("cmd line parse failed: " + e.getMessage());

            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("zookeeper [OPTIONS] [target]...", options);
            System.exit(1);
        } catch (Throwable e) {
            e.printStackTrace();
            System.exit(1);
        }

        System.exit(0);
    }
}
