package com.devsmart.zookeeper.plugins;

import com.devsmart.zookeeper.DefaultProjectVisitor;
import com.devsmart.zookeeper.Platform;
import com.devsmart.zookeeper.Project;
import com.devsmart.zookeeper.projectmodel.*;
import com.devsmart.zookeeper.tasks.BuildTask;
import com.devsmart.zookeeper.tasks.CompileChildProcessTask;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;

public class GCCStaticLibVisitor extends DefaultProjectVisitor {

    private static final Logger LOGGER = LoggerFactory.getLogger(GCCStaticLibVisitor.class);

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
        buildTask.setDelegate(linkDelegate);
        buildTask.setOutput(project.file(new File(genBuildDir(), lib.getName() + ".a")));
        project.addLibBuildTask(buildTask);

        BuildTask resolveDependencyTask = createResolveDeps(lib, buildTask);
        project.getZooKeeper().dependencyGraph.addTask(resolveDependencyTask);

        GnuCompilerVisitor cppVisitor = new GnuCompilerVisitor();
        cppVisitor.compilerCmd = cppCmd;
        cppVisitor.compileSettings = cppSettings;
        cppVisitor.fileFilter = new RegexFileFilter(".*\\.cpp|cc$");
        cppVisitor.project = project;
        cppVisitor.buildTask = buildTask;
        cppVisitor.platform = platform;
        cppVisitor.variant = variant;
        cppVisitor.extra = "staticLib";
        cppVisitor.resolveDependencyTask = resolveDependencyTask;
        cppVisitor.visit(lib);

        GnuCompilerVisitor cVisitor = new GnuCompilerVisitor();
        cVisitor.compilerCmd = cCmd;
        cVisitor.compileSettings = cSettings;
        cVisitor.fileFilter = new RegexFileFilter(".*\\.c$");
        cVisitor.project = project;
        cVisitor.buildTask = buildTask;
        cVisitor.platform = platform;
        cVisitor.variant = variant;
        cVisitor.extra = "staticLib";
        cVisitor.resolveDependencyTask = resolveDependencyTask;
        cVisitor.visit(lib);

    }

    private BuildTask createResolveDeps(final BuildableModule m, final CompileChildProcessTask buildTask) {
        return new BuildTask() {
            @Override
            public boolean run() {
                for(Library childLib : m.getDependencies()) {
                    Module module = project.resolveLibrary(childLib, platform);
                    if(module == null) {
                        LOGGER.error("can not resolve library: [{}:{}] needed to build: {}", childLib, platform, m.getName());
                        return false;
                    } else {
                        if(module instanceof BuildableLibrary) {
                            //TODO: add graph dependency
                        } else if(module instanceof PrecompiledLibrary) {
                            PrecompiledLibrary childPrecompiledLib = (PrecompiledLibrary) module;
                            buildTask.addModifier(createDepLibModifier(childPrecompiledLib));
                        }
                    }
                }
                return true;
            }
        };
    }

    private CompileProcessModifier createDepLibModifier(PrecompiledLibrary childPrecompiledLib) {
        return new CompileProcessModifier() {
            @Override
            public void apply(CompileChildProcessTask ctx) {
                ctx.getCompileContext().sharedLinkedLibs.add(childPrecompiledLib);
            }
        };
    }

    private String genTaskName() {
        return "staticLib"
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
            ArrayList<String> cmdline = new ArrayList<>();
            cmdline.add(linkCmd);
            cmdline.add("rcs");

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
