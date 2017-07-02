package com.devsmart.zookeeper.action;


import com.devsmart.zookeeper.Action;
import com.devsmart.zookeeper.ast.Nodes;
import com.google.common.base.Charsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

public class GenerateCMakeFile implements Action {

    private static final Logger LOGGER = LoggerFactory.getLogger(GenerateCMakeFile.class);

    public Nodes.LibNode mLibrary;
    public BufferedWriter writer;
    public File mProjectRootDir;



    @Override
    public void doIt() {

        try {
            String versionStr = String.format("%d.%d.%d",
                    mLibrary.library.version.major,
                    mLibrary.library.version.minor,
                    mLibrary.library.version.revision
            );

            writer.write("cmake_minimum_required(VERSION 3.2 FATAL_ERROR)");
            writer.newLine();
            writer.newLine();
            writer.write("project(" + mLibrary.library.name + " VERSION " + versionStr + " LANGUAGES CXX C)");
            writer.newLine();
            writer.newLine();
            writer.write("OPTION(BUILD_TESTS \"Build unit tests\" ON)");
            writer.newLine();
            writer.newLine();
            writeTargetIncludeDirs(writer);
            writer.newLine();
            writer.newLine();
            writeAddLibrary(writer);
            writer.newLine();

            writer.flush();

        } catch (IOException e) {
          LOGGER.error("", e);
        }


    }

    private static final Comparator<File> LEX_FILE_COMPARATOR = new Comparator<File>() {

        @Override
        public int compare(File o1, File o2) {
            return o1.getName().compareTo(o2.getName());
        }
    };

    private void writeAddLibrary(BufferedWriter writer) throws IOException {
        File srcDir = new File(mProjectRootDir, "src");
        List<File> srcFileList = new LinkedList<File>();
        for(File srcFile : srcDir.listFiles()) {
            final String fileName = srcFile.getName();
            if(fileName.endsWith(".cpp")
                    || fileName.endsWith(".cc")
                    || fileName.endsWith(".c")
                    || fileName.endsWith(".h")) {
                srcFileList.add(srcFile);
            }
        }

        Collections.sort(srcFileList, LEX_FILE_COMPARATOR);


        writer.write("add_library(${PROJECT_NAME}");
        for(File f : srcFileList) {
            writer.newLine();
            writer.write("src/" + f.getName());
        }
        writer.newLine();
        writer.write(")");

    }

    private void writeTargetIncludeDirs(BufferedWriter writer) throws IOException {
        writer.write("target_include_directories(${PROJECT_NAME} PUBLIC");
        writer.newLine();
        writer.write("  $<BUILD_INTERFACE:${CMAKE_CURRENT_SOURCE_DIR}/include>");
        writer.newLine();
        writer.write("  $<INSTALL_INTERFACE:include>");
        writer.newLine();
        writer.write("  PRIVATE src");
        writer.newLine();
        writer.write(")");
    }
}
