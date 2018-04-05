package com.devsmart.zookeeper.tasks;

import com.google.common.base.Joiner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class ChildProcessTask extends BasicTask implements BuildTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(ChildProcessTask.class);
    private static final ExecutorService IOTHREADS = Executors.newCachedThreadPool();

    public interface DeferredMap {
        void call(Map<String, String> input);
    }

    abstract String[] getCommandLine();

    public File getWorkingDir() {
        return null;
    }

    public void updateEnv(Map<String, String> env) {
    }

    @Override
    public boolean run() {
        try {

            String[] cmdLine = getCommandLine();
            File dir = getWorkingDir();

            LOGGER.info("run: {}", Joiner.on(" ").join(cmdLine));

            ProcessBuilder builder = new ProcessBuilder()
                    .command(cmdLine)
                    .redirectErrorStream(true)
                    .redirectOutput(ProcessBuilder.Redirect.PIPE)
                    .directory(dir);

            Map<String, String> childEnv = builder.environment();

            updateEnv(childEnv);

            Process childProcess = builder.start();

            InputStream inputStream = childProcess.getInputStream();
            IOTHREADS.execute(createStreamTask(inputStream));

            final int exitCode = childProcess.waitFor();

            final boolean success = exitCode == 0;
            if(!success) {
                LOGGER.warn("process ended with status code: {}", exitCode);
            }
            return success;

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
