package com.devsmart.zookeeper;


import com.devsmart.zookeeper.action.PhonyAction;
import com.devsmart.zookeeper.ast.Nodes;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.apache.commons.cli.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Locale;

public class ZooKeeper {

    public DependencyGraph mDependencyGraph = new DependencyGraph();
    public File mZooKeeperRoot;
    public ArrayList<Library> mAllLibraries = new ArrayList<Library>();

    ZooKeeper() {
        mZooKeeperRoot = new File(System.getProperty("user.home"));
        mZooKeeperRoot = new File(mZooKeeperRoot, ".zookeeper");
        mZooKeeperRoot.mkdirs();
    }

    public File getInstallDir(Library library, Platform platform) {
        File retval = new File(mZooKeeperRoot, "install");
        retval = new File(retval, String.format("%s-%s", library.name, library.version));
        retval = new File(retval, platform.toString());
        return retval;
    }

    public static Platform getNativePlatform() {
        Platform.OS os;
        String OSString = System.getProperty("os.name", "generic").toLowerCase(Locale.ENGLISH);
        if (OSString.contains("mac") || OSString.contains("darwin")) {
            os = Platform.OS.MACOSX;
        } else if (OSString.contains("win")) {
            os = Platform.OS.WINDOWS;
        } else if (OSString.contains("nux")) {
            os = Platform.OS.LINUX;
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
        compilerContext.allLibraries = mAllLibraries;

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

        PhonyAction checkAllLibsAction = new PhonyAction();
        mDependencyGraph.addAction("check", checkAllLibsAction);
        for(Library lib : mAllLibraries) {
            Action checkLib = mDependencyGraph.getAction("check"+lib.name);
            mDependencyGraph.addDependency(checkAllLibsAction, checkLib);
        }

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
                ZooKeeper zoo = new ZooKeeper();
                zoo.compileFile(inputFile);
            } else {
                System.err.println("could not open file: " + inputFile.getAbsolutePath());
            }


        } catch (ParseException e) {
            System.err.println("cmd line parse failed: " + e.getMessage());

            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("app [OPTIONS] [FILE]..", options);
            System.exit(-1);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }
}
