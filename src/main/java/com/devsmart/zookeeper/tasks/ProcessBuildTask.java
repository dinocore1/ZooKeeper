package com.devsmart.zookeeper.tasks;


import com.devsmart.ThreadUtils;
import com.google.common.base.Joiner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ProcessBuildTask extends BuildArtifact {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProcessBuildTask.class);


    public List<String> commandLine = new ArrayList<String>();
    public File mExeDir;

    @Override
    public boolean run() {
        try {

            LOGGER.info("run: {}", Joiner.on(" ").join(commandLine));

            ProcessBuilder builder = new ProcessBuilder()
                    .command(commandLine)
                    .redirectErrorStream(true)
                    .redirectOutput(ProcessBuilder.Redirect.PIPE)
                    .directory(mExeDir)
            ;

            Map<String, String> env = builder.environment();

            //String path = "C:\\Users\\pauls\\.zookeeper\\toolchains\\mingw64\\bin;" + env.get("Path");
            //env.clear();
            //env.put("Path", path);

            Process childProcess = builder.start();

            InputStream inputStream = childProcess.getInputStream();
            ThreadUtils.IOThreads.execute(createStreamTask(inputStream));

            final int exitCode = childProcess.waitFor();

            return exitCode == 0;

        } catch (Exception e) {
            LOGGER.error("", e);
            return false;
        }

    }

    private Runnable createStreamTask(final InputStream inputStream) {
        return new Runnable() {
            @Override
            public void run() {
                try {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

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
