package com.devsmart.zookeeper.action;


import com.devsmart.zookeeper.Version;
import com.devsmart.zookeeper.ast.Nodes;
import com.google.common.base.Charsets;
import org.junit.Test;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.OutputStreamWriter;

public class GenerateCMakeFileTest {

    @Test
    public void generateCMakeFileForLibrary() throws Exception {
        GenerateCMakeFile generateCMakeFile = new GenerateCMakeFile();
        generateCMakeFile.mLibrary = new Nodes.LibNode("example", Version.fromString("0.1.0"));

        generateCMakeFile.mProjectRootDir = new File("example");
        generateCMakeFile.mProjectRootDir = new File(generateCMakeFile.mProjectRootDir, "library");

        generateCMakeFile.writer = new BufferedWriter(null);
        generateCMakeFile.doIt();
        generateCMakeFile.outputStream.close();



    }
}
