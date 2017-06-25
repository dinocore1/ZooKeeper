package com.devsmart.zookeeper;


import com.devsmart.zookeeper.ast.Nodes;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.apache.commons.cli.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class ZooKeeper {

    private Nodes.Node mFileNode;

    ZooKeeper() {

    }

    public boolean compileInputStream(ANTLRInputStream inputStream) {
        CompilerContext compilerContext = new CompilerContext();

        ZooKeeperLexer lexer = new ZooKeeperLexer(inputStream);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        ZooKeeperParser parser = new ZooKeeperParser(tokens);
        parser.addErrorListener(compilerContext.parserErrorHandler);

        ZooKeeperParser.FileContext root = parser.file();

        SemPass1 semPass1 = new SemPass1(compilerContext);
        mFileNode = semPass1.visitFile(root);

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

        return true;
    }

    public boolean compileFile(File file) throws IOException {
        FileInputStream fin = new FileInputStream(file);
        ANTLRInputStream inputStream = new ANTLRInputStream(fin);
        inputStream.name = file.getAbsolutePath();
        final boolean success = compileInputStream(inputStream);

        return success;
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
