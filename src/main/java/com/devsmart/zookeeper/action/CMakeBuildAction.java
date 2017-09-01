package com.devsmart.zookeeper.action;

import com.devsmart.ThreadUtils;
import com.devsmart.zookeeper.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;


public class CMakeBuildAction implements Action {

    private static final Logger LOGGER = LoggerFactory.getLogger(CMakeBuildAction.class);

    public static String createActionName(LibraryPlatformKey key) {
        return Utils.createActionName("build", key.toString());
    }

    public final CMakeBuildContext context;

    public CMakeBuildAction(CMakeBuildContext context) {
        this.context = context;
    }

    @Override
    public void doIt() throws Exception {
        final File buildDir = context.buildDir.get();

        ArrayList<String> commandLine = new ArrayList<String>();
        commandLine.add(context.resolveVar(ZooKeeper.VAR_CMAKE_EXE));
        commandLine.add("--build");
        commandLine.add(buildDir.getAbsolutePath());

        Process childProcess;
        ProcessBuilder builder = new ProcessBuilder()
                .command(commandLine)
                .redirectErrorStream(true)
                .redirectOutput(ProcessBuilder.Redirect.PIPE)
                .directory(buildDir);


        childProcess = builder.start();
        ThreadUtils.IOThreads.execute(Utils.createInputStreamLogAppender(childProcess.getInputStream(), LOGGER));
        childProcess.waitFor();

        int exitCode = childProcess.exitValue();
        if(exitCode != 0) {
            throw new RuntimeException("cmake build process exited with code: " + exitCode);
        }


    }
}
