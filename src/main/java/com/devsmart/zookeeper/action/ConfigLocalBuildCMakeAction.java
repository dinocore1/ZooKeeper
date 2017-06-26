package com.devsmart.zookeeper.action;


import com.devsmart.ThreadUtils;
import com.devsmart.zookeeper.Action;
import com.devsmart.zookeeper.Library;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

public class ConfigLocalBuildCMakeAction implements Action {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigLocalBuildCMakeAction.class);

    public final Set<Library> libraryDependencies = new HashSet<Library>();
    public LinkedHashSet<String> cmakeArgs = new LinkedHashSet<String>();
    public File rootProjectDir;
    public File buildDir;

    @Override
    public void doIt() {
        try {
            buildDir.mkdirs();
            doConfig();
        }catch (Exception e) {
            LOGGER.error("", e);
        }
    }

    static final Function<File, String> FILE_TO_STRING = new Function<File, String>() {

        @Override
        public String apply(File input) {
            return input.getAbsolutePath();
        }
    };

    static void addCMakeLibraryArgs(List<String> commandLine, Library lib) {
        String includeDirList = Joiner.on(';').join(Iterables.transform(lib.includePaths, FILE_TO_STRING));
        commandLine.add(String.format("-D%s_INCLUDE_DIRS=\"%s\"", lib.name, includeDirList));

        String librariesList = Joiner.on(';').join(Iterables.transform(lib.linkLibPaths, FILE_TO_STRING));
        commandLine.add(String.format("-D%s_LIBRARIES=\"%s\"", lib.name, librariesList));
    }

    void doConfig() throws Exception {
        List<String> commandLine = new ArrayList<String>();
        commandLine.add("cmake");
        commandLine.add(rootProjectDir.getAbsolutePath());

        for(Library lib : libraryDependencies) {
            addCMakeLibraryArgs(commandLine, lib);
        }

        for(String arg : cmakeArgs) {
            commandLine.add("-D" + arg);
        }

        Process configProcess;
        ProcessBuilder builder = new ProcessBuilder()
                .command(commandLine)
                .redirectErrorStream(true)
                .redirectOutput(ProcessBuilder.Redirect.PIPE)
                .directory(buildDir);

        configProcess = builder.start();

        ThreadUtils.IOThreads.execute(createStreamTask(configProcess.getInputStream()));
        configProcess.waitFor();
    }

    private Runnable createStreamTask(final InputStream in) {
        return new Runnable() {
            @Override
            public void run() {
                try {

                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));

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
