package com.devsmart.zookeeper.action;


import com.devsmart.ThreadUtils;
import com.devsmart.zookeeper.Action;
import com.devsmart.zookeeper.Library;
import com.devsmart.zookeeper.Platform;
import com.devsmart.zookeeper.Utils;
import com.google.common.base.CaseFormat;
import com.google.common.io.BaseEncoding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;
import java.util.concurrent.Callable;

public class BuildCMakeLibAction implements Action {

    private static final Logger LOGGER = LoggerFactory.getLogger(BuildCMakeLibAction.class);

    public static class ExternalLibrary {
        public Library library;
        public File cmakeExportDir;
    }

    public File rootDir;

    public File installDir;
    public Callable<File> installDirCallable;

    public Iterable<ExternalLibrary> externalLibraries = Collections.emptyList();
    public Callable<Iterable<ExternalLibrary>> externalLibrariesCallable;

    public LinkedHashSet<String> cmakeArgs = new LinkedHashSet<String>();


    private File mBuildDir;

    public static String createActionName(Library lib) {
        return "build" + Utils.captialFirstLetter(lib.name);
    }

    public static String createActionName(Library lib, Platform platform) {
        return "build" + Utils.captialFirstLetter(lib.name) + Utils.captialFirstLetter(platform.toString());
    }

    @Override
    public void doIt() {
        try {
            if(installDirCallable != null) {
                installDir = installDirCallable.call();
            }
            installDir.mkdirs();

            if(externalLibrariesCallable != null) {
                externalLibraries = externalLibrariesCallable.call();
            }

            mBuildDir = createTmpBuildDir();

            doConfig();
            doInstall();

        } catch (Exception e) {
            LOGGER.error("", e);
        } finally {
            deleteRecursive(mBuildDir);
        }
    }

    void doConfig() throws Exception {
        List<String> commandLine = new ArrayList<String>();
        commandLine.add("cmake");
        commandLine.add(rootDir.getAbsolutePath());

        for(String arg : cmakeArgs) {
            commandLine.add("-D" + arg);
        }

        commandLine.add("-DCMAKE_INSTALL_PREFIX=" + installDir.getAbsolutePath() + "");

        for(ExternalLibrary externalLibrary : externalLibraries) {
            commandLine.add(String.format("-D%s_DIR=%s",
                    externalLibrary.library.name,
                    externalLibrary.cmakeExportDir.getAbsolutePath()
                    ));
        }

        Process configProcess;
        ProcessBuilder builder = new ProcessBuilder()
                .command(commandLine)
                .redirectErrorStream(true)
                .redirectOutput(ProcessBuilder.Redirect.PIPE)
                .directory(mBuildDir);

        configProcess = builder.start();

        ThreadUtils.IOThreads.execute(createStreamTask(configProcess.getInputStream()));
        configProcess.waitFor();
    }

    void doInstall() throws Exception {
        ArrayList<String> commandLine = new ArrayList<String>();
        commandLine.add("cmake");
        commandLine.add("--build");
        commandLine.add(mBuildDir.getAbsolutePath());
        commandLine.add("--target");
        commandLine.add("install");

        Process buildProcess;
        ProcessBuilder builder = new ProcessBuilder()
                .command(commandLine)
                .redirectErrorStream(true)
                .redirectOutput(ProcessBuilder.Redirect.PIPE)
                .directory(mBuildDir);


        buildProcess = builder.start();
        ThreadUtils.IOThreads.execute(createStreamTask(buildProcess.getInputStream()));
        buildProcess.waitFor();
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

    private File createTmpBuildDir() {
        File dir = new File(".zoo");
        dir = new File(dir, "builds");

        Random random = new Random();
        byte[] data = new byte[3];
        random.nextBytes(data);
        dir = new File(dir, BaseEncoding.base16().encode(data));
        dir.mkdirs();
        return dir;
    }

    public void deleteRecursive(File path){
        File[] c = path.listFiles();
        for (File file : c){
            if (file.isDirectory()){
                deleteRecursive(file);
                file.delete();
            } else {
                file.delete();
            }
        }
        path.delete();
    }
}
