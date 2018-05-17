package com.devsmart.zookeeper.plugins;

import com.devsmart.zookeeper.DefaultProjectVisitor;
import com.devsmart.zookeeper.LinkableLibrary;
import com.devsmart.zookeeper.Platform;
import com.devsmart.zookeeper.Project;
import com.devsmart.zookeeper.projectmodel.*;
import com.devsmart.zookeeper.tasks.CompileChildProcessTask;
import com.devsmart.zookeeper.tasks.CreatePackageTask;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class GCCSharedLibVisitor extends DefaultProjectVisitor {

    private static final Logger LOGGER = LoggerFactory.getLogger(GCCSharedLibVisitor.class);

    public Platform platform;
    public String variant;

    public String cppCmd;
    public CompileSettings cppSettings;

    public String cCmd;
    public CompileSettings cSettings;

    public String linkCmd;

    public String filenameExtendtion = ".so";

    protected BuildableLibrary library;
    protected CompileChildProcessTask buildTask;
    protected Project project;
    protected CreatePackageTask createPackageTask;

    @Override
    public void visit(Project project) {
        this.project = project;
        super.visit(project);
    }

    @Override
    public void visit(BuildableExecutable exe) {
    }

    @Override
    public void visit(BuildableLibrary lib) {
        library = lib;

        buildTask = new CompileChildProcessTask();
        buildTask.getCompileContext().module = lib;
        buildTask.setName(genTaskName());
        buildTask.setOutput(project.file(new File(genBuildDir(), lib.getName() + filenameExtendtion)));
        buildTask.setDelegate(linkDelegate);
        project.addLibBuildTask(buildTask);

        project.addDoLast(createResolveDeps(library));

        createPackageTask = new CreatePackageTask(lib, platform);
        createPackageTask.setOutput(project.file(new File(genBuildDir(), "package/lib.zoo")));
        createPackageTask.setName("package" + StringUtils.capitalize(library.getName())
                + StringUtils.capitalize(platform.toString())
                + StringUtils.capitalize(variant));
        createPackageTask.libFileList.add("bin/" + lib.getName() + filenameExtendtion);



        cppSettings.getFlags().add("-fPIC");

        GnuCompilerVisitor cppVisitor = new GnuCompilerVisitor();
        cppVisitor.compilerCmd = cppCmd;
        cppVisitor.compileSettings = cppSettings;
        cppVisitor.fileFilter = new RegexFileFilter(".*\\.cpp|cc$");
        cppVisitor.project = project;
        cppVisitor.buildTask = buildTask;
        cppVisitor.platform = platform;
        cppVisitor.variant = variant;
        cppVisitor.extra = "sharedLib";
        cppVisitor.visit(lib);

        cSettings.getFlags().add("-fPIC");

        GnuCompilerVisitor cVisitor = new GnuCompilerVisitor();
        cVisitor.compilerCmd = cCmd;
        cVisitor.compileSettings = cSettings;
        cVisitor.fileFilter = new RegexFileFilter(".*\\.c$");
        cVisitor.project = project;
        cVisitor.buildTask = buildTask;
        cVisitor.platform = platform;
        cVisitor.variant = variant;
        cVisitor.extra = "sharedLib";
        cVisitor.visit(lib);

    }

    private Runnable createResolveDeps(final BuildableModule m) {
        return new Runnable() {
            @Override
            public void run() {
                for(Library childLib : m.getDependencies()) {
                    Module module = project.resolveLibrary(childLib, platform);
                    if(module == null) {
                        LOGGER.error("can not resolve library: {}", childLib);
                    } else {


                    }
                }
            }
        };
    }

    private String genTaskName() {
        return "sharedLib"
                + StringUtils.capitalize(library.getName())
                + StringUtils.capitalize(platform.toString())
                + StringUtils.capitalize(variant);
    }

    File genBuildDir() {
        File buildDir = new File(project.getProjectDir(), "build");
        buildDir = new File(buildDir, platform.toString());
        buildDir = new File(buildDir, variant);
        return buildDir;
    }

    private CompileChildProcessTask.Delegate linkDelegate = new CompileChildProcessTask.Delegate() {

        @Override
        public String[] getCommandLine(CompileChildProcessTask task) {
            task.doModify();
            CompileContext compileContext = task.getCompileContext();

            compileContext.flags.add("-shared");

            ArrayList<String> cmdline = new ArrayList<>();
            cmdline.add(linkCmd);
            cmdline.addAll(compileContext.flags);

            cmdline.add("-o");
            cmdline.add(task.getOutput().getSingleFile().getAbsoluteFile().toString());

            for(File input : task.getInput()) {
                cmdline.add(input.getAbsolutePath().toString());
            }

            LinkedHashSet<File> librarySearchPaths = new LinkedHashSet<>();
            LinkedHashSet<String> linkLibNames = new LinkedHashSet<>();


            GCCHelper.libraryLinkerLine(project, library.getDependencies(), platform, librarySearchPaths, linkLibNames);

            for(File linkSearchPath : librarySearchPaths) {
                cmdline.add("-L" + linkSearchPath.getAbsolutePath());
            }

            for(String libName : linkLibNames) {
                cmdline.add("-l" + libName);
            }

            return cmdline.toArray(new String[cmdline.size()]);
        }

        @Override
        public File getWorkingDir(CompileChildProcessTask task) {
            return null;
        }

        @Override
        public void updateEnv(CompileChildProcessTask task, Map<String, String> env) {

        }
    };
}
