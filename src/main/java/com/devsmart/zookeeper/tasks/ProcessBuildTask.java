package com.devsmart.zookeeper.tasks;


import com.devsmart.ThreadUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class ProcessBuildTask extends BuildArtifact {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProcessBuildTask.class);


    public ArrayList<String> commandLine = new ArrayList<String>();
    public File mExeDir;

    @Override
    public boolean run() {
        try {
            Process process = new ProcessBuilder(commandLine)
                    .redirectErrorStream(true)
                    .redirectOutput(ProcessBuilder.Redirect.PIPE)
                    .directory(mExeDir)
                    .start();

            ThreadUtils.IOThreads.execute(createStreamTask(process));

            final int exitCode = process.waitFor();

            return exitCode == 0;

        } catch (Exception e) {
            LOGGER.error("", e);
            return false;
        }

    }

    private Runnable createStreamTask(final Process process) {
        return new Runnable() {
            @Override
            public void run() {
                try {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

                    String line;
                    while((line = reader.readLine()) != null) {
                        LOGGER.info(">> " + line);
                    }

                } catch (Exception e) {
                    LOGGER.error("", e);
                }
            }
        };
    }
}
