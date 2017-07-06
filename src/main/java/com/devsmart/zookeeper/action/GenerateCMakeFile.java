package com.devsmart.zookeeper.action;


import com.devsmart.zookeeper.Action;
import com.devsmart.zookeeper.Library;
import com.devsmart.zookeeper.Utils;
import com.devsmart.zookeeper.ast.Nodes;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;

public class GenerateCMakeFile implements Action {

    private static final Logger LOGGER = LoggerFactory.getLogger(GenerateCMakeFile.class);
    private static final ImmutableSet<String> SOURCE_POSTFIXES = ImmutableSet.of(".cpp", ".cc", ".c", ".h");

    public static String createActionName(Library library) {
        return "genCMake" + Utils.captialFirstLetter(library.name);
    }

    public Nodes.LibNode mLibrary;
    public File mProjectRootDir;
    public File mOutputFile;


    @Override
    public void doIt() {

        try {
            LOGGER.info("Generating CMake file for:{} {}", mLibrary.library, mOutputFile.getAbsolutePath());

            BufferedWriter writer = new BufferedWriter(new FileWriter(mOutputFile));

            String versionStr = String.format("%d.%d.%d",
                    mLibrary.library.version.major,
                    mLibrary.library.version.minor,
                    mLibrary.library.version.patch
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
            writeFindCompileDependencies(writer);
            writer.newLine();
            writer.newLine();
            writeAddLibrary(writer);
            writer.newLine();
            writer.newLine();
            writeTargetIncludeDirs(writer);
            writer.newLine();
            writer.newLine();
            writeInstall(writer);
            writer.newLine();

            writer.close();

        } catch (IOException e) {
          LOGGER.error("", e);
        }


    }

    private void writeInstall(BufferedWriter writer) throws IOException {
        writer.newLine();
        writer.write("install(TARGETS " + mLibrary.library.name);
        writer.newLine();
        writer.write("  EXPORT " + mLibrary.library.name +"Config");
        writer.newLine();
        writer.write("  RUNTIME DESTINATION bin");
        writer.newLine();
        writer.write("  LIBRARY DESTINATION lib");
        writer.newLine();
        writer.write("  ARCHIVE DESTINATION lib/static");
        writer.newLine();
        writer.write(")");

        writer.newLine();
        writer.newLine();

        writer.write("install(DIRECTORY include/");
        writer.newLine();
        writer.write("  DESTINATION include");
        writer.newLine();
        writer.write(")");

        writer.newLine();
        writer.newLine();

        writer.write("install(EXPORT " + mLibrary.library.name + "Config");
        writer.newLine();
        writer.write("  DESTINATION cmake");
        writer.newLine();
        writer.write(")");
    }

    private void writeFindCompileDependencies(BufferedWriter writer) throws IOException {
        for(Library depenLib : mLibrary.compileLibDependencies) {
            writer.newLine();
            writer.write("find_package(" + depenLib.name + " REQUIRED)");
        }

    }

    private static boolean isSourceFile(File f) {
        if(f.isFile()) {
            final String fileName = f.getName();
            for (String postfix : SOURCE_POSTFIXES) {
                if (fileName.endsWith(postfix)) {
                    return true;
                }
            }
        }
        return false;
    }

    private void addSourceFiles(Collection<String> sourceSet, String[] prefix, File file) {
        if(file.isDirectory()) {
            prefix = Arrays.copyOf(prefix, prefix.length+1);
            prefix[prefix.length-1] = file.getName();
            for(File f : file.listFiles()) {
                addSourceFiles(sourceSet, prefix, f);
            }
        } else if(isSourceFile(file)){
            String[] path = Arrays.copyOf(prefix, prefix.length+1);
            path[path.length-1] = file.getName();
            sourceSet.add(Joiner.on('/').join(path));
        }
    }

    private void writeAddLibrary(BufferedWriter writer) throws IOException {
        TreeSet<String> srcFileList = new TreeSet<String>();
        addSourceFiles(srcFileList, new String[0], new File(mProjectRootDir, "src"));
        addSourceFiles(srcFileList, new String[0], new File(mProjectRootDir, "include"));

        writer.write("add_library(" + mLibrary.library.name);
        for(String filePath : srcFileList) {
            writer.newLine();
            writer.write("  " + filePath);
        }
        writer.newLine();
        writer.write(")");

    }

    private void writeTargetIncludeDirs(BufferedWriter writer) throws IOException {
        writer.write("target_include_directories(" + mLibrary.library.name);
        writer.newLine();
        writer.write("  PUBLIC $<BUILD_INTERFACE:${CMAKE_CURRENT_SOURCE_DIR}/include>");
        writer.newLine();
        writer.write("  $<INSTALL_INTERFACE:include>");
        writer.newLine();
        writer.write("  PRIVATE src");
        writer.newLine();
        writer.write(")");
    }
}
