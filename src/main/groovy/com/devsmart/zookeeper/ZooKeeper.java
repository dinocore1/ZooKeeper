package com.devsmart.zookeeper;


import com.devsmart.IOUtils;
import com.devsmart.zookeeper.ast.Nodes;
import com.devsmart.zookeeper.tasks.BuildTask;
import com.google.common.collect.ComparisonChain;
import com.google.common.hash.HashCode;
import com.google.common.io.BaseEncoding;
import com.google.common.io.Resources;
import com.google.common.primitives.SignedBytes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.apache.commons.cli.*;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.jetbrains.annotations.NotNull;
import org.mapdb.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.TreeMap;

public class ZooKeeper {

    private static final Logger LOGGER = LoggerFactory.getLogger(ZooKeeper.class);

    public static final String VAR_CMAKE_EXE = "CMAKE_EXE";
    public static final String PROJECT_DIR = "PROJECT_DIR";
    public static final String ZOOKEEPER_HOME = "ZOOKEEPER_HOME";

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
    public BuildManager mBuildManager;
    public DependencyGraph mDependencyGraph = new DependencyGraph();
    public File mZooKeeperRoot;
    public ArrayList<Library> mAllLibraries = new ArrayList<Library>();
    public TreeMap<LibraryPlatformKey, HashCode> mLibraryHashTable = new TreeMap<LibraryPlatformKey, HashCode>();

    void readCompilerConfig() {
        File compilerDir = new File(mZooKeeperRoot, "compilers");
        if(!compilerDir.exists()) {
            compilerDir.mkdirs();
            try {
                File cfgFile = new File(compilerDir, "gcc-linux.cfg");
                IOUtils.pump(Resources.getResource("gcc-linux.cfg").openStream(), new FileOutputStream(cfgFile));
            } catch (IOException e) {
                LOGGER.error("", e);
            }
        }

        for(File compilerCfg : compilerDir.listFiles()) {

            try {

                Binding binding = new Binding();
                CompilerConfiguration cc = new CompilerConfiguration();
                cc.setScriptBaseClass("com.devsmart.zookeeper.ZooKeeperDSL");

                GroovyShell shell = new GroovyShell(ZooKeeperDSL.class.getClassLoader(), binding, cc);

                shell.evaluate(compilerCfg);

                CompileTemplateBuilder compile = (CompileTemplateBuilder) binding.getProperty("compileBuilder");

                //mBuildManager.addCompiler(templates.get(0));

            } catch (Exception e) {
                LOGGER.error("", e);
            }

        }
    }

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
        mVM.setVar(PROJECT_DIR, new File("").getAbsolutePath());
        mVM.setVar(ZOOKEEPER_HOME, mZooKeeperRoot.getAbsolutePath());

        mBuildManager = new BuildManager();
        mBuildManager.mZooKeeper = this;

        readCompilerConfig();
    }

    public File getLocalInstallDir() {
        return new File(mZooKeeperRoot, "install");
    }

    public File getLocalInstallDir(Library library, Platform platform) {
        File localInstallDir = getLocalInstallDir();
        localInstallDir = new File(localInstallDir, library.name);
        localInstallDir = new File(localInstallDir, platform.toString());
        localInstallDir = new File(localInstallDir, library.version.toString());
        return localInstallDir;
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

    public boolean compileInputStream(ANTLRInputStream inputStream, CompilerContext compilerContext) {
        ZooKeeperLexer lexer = new ZooKeeperLexer(inputStream);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        ZooKeeperParser parser = new ZooKeeperParser(tokens);
        parser.addErrorListener(compilerContext.parserErrorHandler);

        ZooKeeperParser.FileContext root = parser.file();

        SemPass1 semPass1 = new SemPass1(compilerContext);
        compilerContext.rootNode = semPass1.visitFile(root);

        if(compilerContext.hasErrors()) {
            compilerContext.reportMessages(System.err);
            return false;
        }

        if(compilerContext.rootNode instanceof Nodes.PrecompiledLibraryDefNode) {
            mBuildManager.addPrecompiledLibrary(compilerContext, (Nodes.PrecompiledLibraryDefNode) compilerContext.rootNode);
        }

        if(compilerContext.rootNode instanceof Nodes.BuildLibraryDefNode) {
            mBuildManager.addBuildLibrary((Nodes.BuildLibraryDefNode) compilerContext.rootNode);
        }

        if(compilerContext.rootNode instanceof Nodes.BuildExeDefNode) {
            mBuildManager.addBuildExe((Nodes.BuildExeDefNode) compilerContext.rootNode);
        }

        return true;
    }

    public CompilerContext createCompilerContext() {
        CompilerContext compilerContext = new CompilerContext();
        compilerContext.dependencyGraph = mDependencyGraph;
        compilerContext.VM = mVM;
        compilerContext.zooKeeper = this;
        return compilerContext;
    }

    public boolean compileInputStream(ANTLRInputStream inputStream, File localDir) {
        CompilerContext compilerContext = createCompilerContext();
        compilerContext.localDir = localDir;
        return compileInputStream(inputStream, compilerContext);
    }

    public boolean compileFile(File file) throws IOException {
        FileInputStream fin = new FileInputStream(file);
        ANTLRInputStream inputStream = new ANTLRInputStream(fin);
        inputStream.name = file.getAbsolutePath();
        return compileInputStream(inputStream, file.getParentFile());
    }

    public boolean compileFile(File file, CompilerContext context) throws IOException {
        FileInputStream fin = new FileInputStream(file);
        ANTLRInputStream inputStream = new ANTLRInputStream(fin);
        inputStream.name = file.getAbsolutePath();
        return compileInputStream(inputStream, context);
    }

    /*
    public boolean compile(InputStream in) throws IOException {
        ANTLRInputStream inputStream = new ANTLRInputStream(in);
        return compileInputStream(inputStream);
    }
    */

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
            for(String target : unparsedArgs) {
                BuildTask task = zoo.mDependencyGraph.getTask(target);
                if(task != null) {
                    ExePlan exeplan = zoo.mDependencyGraph.createExePlan(task);
                    boolean success = exeplan.run(1);
                    System.out.println("Build " + (success ? "success" : "failed"));
                    if(!success) {
                        System.exit(-1);
                    }
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
