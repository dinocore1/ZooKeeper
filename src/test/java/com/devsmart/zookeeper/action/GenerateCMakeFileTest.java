package com.devsmart.zookeeper.action;


import com.devsmart.zookeeper.Version;
import com.devsmart.zookeeper.ast.Nodes;
import com.google.common.base.Charsets;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.*;

public class GenerateCMakeFileTest {

    File genFile;

    @Before
    public void setup() {
        genFile = new File("example");
        genFile = new File(genFile, "library");
        genFile = new File(genFile, "CMakeLists.txt");

    }

    @After
    public void tearDown() {
        //genFile.delete();

    }

    @Test
    public void generateCMakeFileForLibrary() throws Exception {
        GenerateCMakeFile generateCMakeFile = new GenerateCMakeFile();
        generateCMakeFile.mLibrary = new Nodes.LibNode("add", Version.fromString("0.1.0"));

        generateCMakeFile.mProjectRootDir = new File("example");
        generateCMakeFile.mProjectRootDir = new File(generateCMakeFile.mProjectRootDir, "library");

        FileWriter fileWriter = new FileWriter(genFile);
        generateCMakeFile.writer = new BufferedWriter(fileWriter);
        generateCMakeFile.doIt();
        generateCMakeFile.writer.close();


    }
}
