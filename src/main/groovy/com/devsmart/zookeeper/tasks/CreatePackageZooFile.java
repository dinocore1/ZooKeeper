package com.devsmart.zookeeper.tasks;

import com.devsmart.zookeeper.Platform;
import com.devsmart.zookeeper.projectmodel.BuildableLibrary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class CreatePackageZooFile extends BasicTask implements BuildTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(CreatePackageZooFile.class);

    public final BuildableLibrary lib;
    public final Platform platform;
    public final List<String> libFileList = new ArrayList<>();



    public CreatePackageZooFile(BuildableLibrary lib, Platform platform) {
        this.lib = lib;
        this.platform = platform;
    }

    @Override
    public boolean run() {


        try {

            PrintWriter writer = new PrintWriter(new FileWriter(getOutput().getSingleFile()));
            writer.append("precompiledLib {");
            writer.println();
            writer.printf("  name '%s'", lib.getName());
            writer.println();
            writer.printf("  version '%s'", lib.getVersion());
            writer.println();
            writer.printf("  platform '%s'", platform);
            writer.println();
            writer.println();
            writer.print(" include file('include')");
            writer.println();
            for(String libFile : libFileList) {
                writer.printf(" libFile file('%s')", libFile);
                writer.println();
            }
            writer.println('}');

            writer.close();

            return true;

        } catch (IOException e) {
            LOGGER.error("", e);
            return false;
        }



    }
}
