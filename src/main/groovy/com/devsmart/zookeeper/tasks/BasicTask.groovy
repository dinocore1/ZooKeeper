package com.devsmart.zookeeper.tasks

import com.devsmart.zookeeper.FileCollection
import com.devsmart.zookeeper.FileUtils
import com.google.common.base.Joiner
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class BasicTask implements BuildTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(BasicTask.class)

    private static final ExecutorService IOTHREADS = Executors.newCachedThreadPool()

    String name
    Closure cmd
    final Set<String> dependencies = []
    FileCollection input
    FileCollection output
    File workingDir

    def name(String name) {
        setName(name)
    }

    def depends(String taskName) {
        dependencies.add(taskName)
    }

    def depends(String... tasks) {
        dependencies.addAll(task)
    }

    def output(Object... paths) {
        output = FileUtils.from(paths)
    }

    def cmd(Closure cl) {
        setCmd(cl)
    }

    static BasicTask make(Closure cl) {
        BasicTask retval = new BasicTask()
        Closure code = cl.rehydrate(retval, retval, retval)
        code()
        return retval
    }

    @Override
    boolean run() {
        try {

            String[] cmdLine = cmd().flatten({ it ->
                if(it instanceof Closure) {
                    return it()
                } else if(it instanceof FileCollection) {
                    return it.iterator().toList()
                } else {
                    return it
                }

            })

            LOGGER.info("run: {}", Joiner.on(" ").join(cmdLine));

            ProcessBuilder builder = new ProcessBuilder()
                    .command(cmdLine)
                    .redirectErrorStream(true)
                    .redirectOutput(ProcessBuilder.Redirect.PIPE)
                    .directory(workingDir)
            ;

            Map<String, String> env = builder.environment();

            //String path = "C:\\Users\\pauls\\.zookeeper\\toolchains\\mingw64\\bin;" + env.get("Path");
            //env.clear();
            //env.put("Path", path);

            Process childProcess = builder.start();

            InputStream inputStream = childProcess.getInputStream();
            IOTHREADS.execute(createStreamTask(inputStream));

            final int exitCode = childProcess.waitFor();

            return exitCode == 0;

        } catch (Exception e) {
            LOGGER.error("", e);
            return false;
        }
    }

    private Runnable createStreamTask(final InputStream inputStream) {
        return {
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
    }
}
