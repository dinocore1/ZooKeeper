package com.devsmart.zookeeper.tasks;

import com.devsmart.zookeeper.Platform;
import com.devsmart.zookeeper.Version;
import com.devsmart.zookeeper.ast.Nodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

public class PrecompiledLibTemplateTask implements BuildTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(PrecompiledLibTemplateTask.class);


    public Nodes.BuildLibraryDefNode mLibDef;
    public File mOutputFile;
    public Platform mPlatform;


    @Override
    public boolean run() {

        try {
            FileWriter writer = new FileWriter(mOutputFile);

            writer.append("lib ");
            writer.append(mLibDef.libName);
            writer.append(" ");
            formatVersion(writer, mLibDef.versionNode.version);
            writer.append(" {\n");

            writer.append(String.format("  platform: \"%s\"\n", mPlatform.toString()));

            writer.append("}");

            writer.close();

            return true;
        } catch (IOException e) {
            LOGGER.error("", e);
            return false;
        }

    }

    private static void formatVersion(Writer writer, Version version) throws IOException {
        writer.append(String.format("%d.%d.%d", version.major, version.minor, version.patch));
    }
}
