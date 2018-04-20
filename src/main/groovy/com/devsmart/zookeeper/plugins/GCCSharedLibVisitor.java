package com.devsmart.zookeeper.plugins;

import com.devsmart.zookeeper.DefaultProjectVisitor;
import com.devsmart.zookeeper.Platform;
import com.devsmart.zookeeper.Project;
import com.devsmart.zookeeper.projectmodel.BuildableExecutable;
import com.devsmart.zookeeper.projectmodel.BuildableLibrary;
import com.devsmart.zookeeper.tasks.CompileChildProcessTask;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;

public class GCCSharedLibVisitor extends DefaultProjectVisitor {

    public Platform platform;
    public String variant;

    public String cppCmd;
    public CompileSettings cppSettings;

    public String cCmd;
    public CompileSettings cSettings;

    public String linkCmd;

    public String filenameExtendtion = ".so";

    protected BuildableLibrary library;
    protected com.devsmart.zookeeper.tasks.CompileChildProcessTask buildTask;
    protected Project project;

    @Override
    public void visit(Project project) {
        this.project = project;
        super.visit(project);
    }

    @Override
    public void visit(BuildableExecutable exe) {
        throw new UnsupportedOperationException();

    }

    @Override
    public void visit(BuildableLibrary lib) {
        library = lib;

        buildTask = new CompileChildProcessTask();
        buildTask.getCompileContext().module = lib;
        buildTask.setName(genTaskName());
        buildTask.setOutput(project.file(new File(genBuildDir(), lib.getName() + filenameExtendtion)));
        buildTask.setDelegate(linkDelegate);
        project.addTask(buildTask);

        cppSettings.getFlags().add("-fPIC");

        GnuCompilerVisitor cppVisitor = new GnuCompilerVisitor();
        cppVisitor.compilerCmd = cppCmd;
        cppVisitor.compileSettings = new SettingWrapper(cppSettings);
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
        cVisitor.compileSettings = new SettingWrapper(cSettings);
        cVisitor.fileFilter = new RegexFileFilter(".*\\.c$");
        cVisitor.project = project;
        cVisitor.buildTask = buildTask;
        cVisitor.platform = platform;
        cVisitor.variant = variant;
        cVisitor.extra = "sharedLib";
        cVisitor.visit(lib);

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

            compileContext.flags.add("-fPIC");
            compileContext.flags.add("-shared");

            ArrayList<String> cmdline = new ArrayList<>();
            cmdline.add(linkCmd);
            cmdline.addAll(compileContext.flags);

            cmdline.add("-o");
            cmdline.add(task.getOutput().getSingleFile().getAbsoluteFile().toString());

            for(File input : task.getInput()) {
                cmdline.add(input.getAbsolutePath().toString());
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
