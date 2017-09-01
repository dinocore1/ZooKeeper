package com.devsmart.zookeeper.action;

import com.devsmart.IOUtils;
import com.devsmart.ThreadUtils;
import com.devsmart.zookeeper.*;
import com.google.common.base.Joiner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class CMakeConfigAction implements Action {

    private static final Logger LOGGER = LoggerFactory.getLogger(CMakeConfigAction.class);

    public static String createActionName(LibraryPlatformKey key) {
        return Utils.createActionName("config", key.toString());
    }

    public final CMakeBuildContext buildContext;

    public CMakeConfigAction(CMakeBuildContext buildContext) {
        this.buildContext = buildContext;
    }


    @Override
    public void doIt() throws Exception {
        final File buildDir = buildContext.buildDir.get();
        IOUtils.deleteTree(buildDir);
        buildDir.mkdirs();

        List<String> commandLine = new ArrayList<String>();
        commandLine.add(buildContext.resolveVar(ZooKeeper.VAR_CMAKE_EXE));
        commandLine.add(buildContext.sourceDir.get().getAbsolutePath());

        for (String arg : buildContext.cMakeArgs) {
            commandLine.add("-D" + arg);
        }

        commandLine.add("-DCMAKE_INSTALL_PREFIX=" + buildContext.installDir.get().getAbsolutePath() + "");

        for (CMakeBuildContext.ExternalLibrary externalLibrary : buildContext.mExternalLibDependencies) {
            commandLine.add(String.format("-D%s_DIR=%s",
                    externalLibrary.library.name,
                    externalLibrary.cmakeExportDir.getAbsolutePath()
            ));
        }

        LOGGER.debug("running cmake config process: {}", Joiner.on(' ').join(commandLine));

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
        if (exitCode != 0) {
            throw new Exception("cmake config process ended with code: " + exitCode);
        }
    }
}
