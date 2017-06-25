package com.devsmart.zookeeper;


import com.google.common.io.BaseEncoding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;

public class BuildCMakeLibAction implements Action {

    private static final Logger LOGGER = LoggerFactory.getLogger(BuildCMakeLibAction.class);

    public File rootDir;
    public File installDir;
    public LinkedHashSet<String> cmakeArgs = new LinkedHashSet<String>();

    @Override
    public void doIt() {
        File buildDir = createTmpBuildDir();
        try {
            List<String> commandLine = new ArrayList<String>();
            commandLine.add("cmake");
            commandLine.add(rootDir.getAbsolutePath());

            for(String arg : cmakeArgs) {
                commandLine.add("-D" + arg);
            }

            Process configProcess = new ProcessBuilder()
                    .command(commandLine)
                    .directory(buildDir)
                    .redirectErrorStream(true)
                    .start();


            configProcess.waitFor();

        } catch (Exception e) {
            LOGGER.error("", e);
        } finally {
            deleteRecursive(buildDir);
        }
    }

    private File createTmpBuildDir() {
        File dir = new File(".zoo");

        Random random = new Random();
        byte[] data = new byte[3];
        random.nextBytes(data);
        dir = new File(dir, BaseEncoding.base16().encode(data));
        dir.mkdirs();
        return dir;
    }

    public void deleteRecursive(File path){
        File[] c = path.listFiles();
        LOGGER.info("Cleaning out folder:" + path.toString());
        for (File file : c){
            if (file.isDirectory()){
                LOGGER.info("Deleting file:" + file.toString());
                deleteRecursive(file);
                file.delete();
            } else {
                file.delete();
            }
        }
        path.delete();
    }
}
