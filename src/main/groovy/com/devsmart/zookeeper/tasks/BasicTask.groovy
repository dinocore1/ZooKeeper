package com.devsmart.zookeeper.tasks

import com.devsmart.zookeeper.Project
import com.devsmart.zookeeper.api.FileCollection
import com.devsmart.zookeeper.ZooKeeper
import com.devsmart.zookeeper.file.FileUtils
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
    Closure env

    def name(String name) {
        setName(name)
    }

    def depends(String taskName) {
        dependencies.add(taskName)
    }

    def depends(String... tasks) {
        dependencies.addAll(task)
    }

    def output(FileCollection fc) {
        output = fc
    }

    def env(Closure cl) {
        setEnv(cl)
    }

    def workingDir(String pathStr) {
        setWorkingDir(new File(pathStr))
    }

    def cmd(Closure cl) {
        setCmd(cl)
    }

    static BasicTask make(Closure cl, Project project) {
        BasicTask retval = new BasicTask()
        Closure code = cl.rehydrate(retval, project, retval)
        code()
        return retval
    }

    @Override
    boolean run() {
        try {

            Object[] cmdLineObj = cmd().flatten({ it ->
                if(it instanceof Closure) {
                    return it()
                } else if(it instanceof FileCollection) {
                    return it.iterator().toList()
                } else {
                    return it
                }

            })

            File workingDir = getWorkingDir()
            if(workingDir != null && cmdLineObj[0] instanceof File) {
                cmdLineObj[0] = cmdLineObj[0].absoluteFile
            }

            String[] cmdLine = cmdLineObj as String[]

            LOGGER.info("run: {}", Joiner.on(" ").join(cmdLine));

            ProcessBuilder builder = new ProcessBuilder()
                    .command(cmdLine)
                    .redirectErrorStream(true)
                    .redirectOutput(ProcessBuilder.Redirect.PIPE)
                    .directory(workingDir)
            ;

            Map<String, String> childEnv = builder.environment();

            if(env != null) {
                Map<String, Object> modifiedEnv = env()
                modifiedEnv.each { e ->
                    childEnv.put(e.key, e.value.toString())
                }
            }

            //String path = "C:\\Users\\pauls\\.zookeeper\\toolchains\\mingw64\\bin;" + env.get("Path");
            //env.clear();
            //env.put("Path", path);

            Process childProcess = builder.start();

            InputStream inputStream = childProcess.getInputStream();
            IOTHREADS.execute(createStreamTask(inputStream));

            final int exitCode = childProcess.waitFor();

            final boolean success = exitCode == 0;
            if(!success) {
                LOGGER.error("process ended with status code: {}", exitCode)
            }
            return success;

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
