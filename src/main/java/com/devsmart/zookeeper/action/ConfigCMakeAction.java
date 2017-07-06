package com.devsmart.zookeeper.action;

import com.devsmart.ThreadUtils;
import com.devsmart.zookeeper.Action;
import com.devsmart.zookeeper.CMakeBuildContext;
import com.devsmart.zookeeper.Utils;
import com.google.common.base.Joiner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;


public class ConfigCMakeAction implements Action {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigCMakeAction.class);

    public final CMakeBuildContext buildContext;

    public ConfigCMakeAction(CMakeBuildContext buildContext) {
        this.buildContext = buildContext;
    }


    @Override
    public void doIt() throws Exception {
        List<String> commandLine = new ArrayList<String>();
        commandLine.add("cmake");
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

        Process configProcess;
        ProcessBuilder builder = new ProcessBuilder()
                .command(commandLine)
                .redirectErrorStream(true)
                .redirectOutput(ProcessBuilder.Redirect.PIPE)
                .directory(buildContext.buildDir.get());

        configProcess = builder.start();

        ThreadUtils.IOThreads.execute(Utils.createInputStreamLogAppender(configProcess.getInputStream(), LOGGER));
        configProcess.waitFor();

        int exitCode = configProcess.exitValue();
        if (exitCode != 0) {
            throw new Exception("cmake config process ended with code: " + exitCode);
        }
    }
}
