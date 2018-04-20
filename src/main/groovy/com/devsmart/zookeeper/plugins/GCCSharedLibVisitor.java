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

    protected BuildableLibrary library;
    protected com.devsmart.zookeeper.tasks.CompileChildProcessTask buildTask;
    protected Project project;

    @Override
    public void visit(Project project) {
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
        buildTask.setDelegate(linkDelegate);
        project.addTask(buildTask);

        //TODO: get all the dependencies and apply them to the build tasks

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

        GnuCompilerVisitor cVisitor = new GnuCompilerVisitor();
        cppVisitor.compilerCmd = cCmd;
        cppVisitor.compileSettings = new SettingWrapper(cSettings);
        cppVisitor.fileFilter = new RegexFileFilter(".*\\.c$");
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

    private CompileChildProcessTask.Delegate linkDelegate = new CompileChildProcessTask.Delegate() {

        @Override
        public String[] getCommandLine(CompileChildProcessTask task) {
            CompileContext compileContext = task.getCompileContext();
            ArrayList<String> cmdline = new ArrayList<>();
            cmdline.add(linkCmd);
            cmdline.addAll(compileContext.flags);

            cmdline.add("-o");
            cmdline.add(task.getOutput().getSingleFile().getAbsoluteFile().toString());
            cmdline.add(task.getInput().getSingleFile().getAbsoluteFile().toString());
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
