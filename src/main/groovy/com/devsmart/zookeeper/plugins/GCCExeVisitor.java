package com.devsmart.zookeeper.plugins;

import com.devsmart.zookeeper.DefaultProjectVisitor;
import com.devsmart.zookeeper.Platform;
import com.devsmart.zookeeper.Project;
import com.devsmart.zookeeper.StringContext;
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

public class GCCExeVisitor extends DefaultProjectVisitor {

    private static final Logger LOGGER = LoggerFactory.getLogger(GCCExeVisitor.class);

    public Platform platform;
    public String variant;

    public String cppCmd;
    public CompileSettings cppSettings;

    public String cCmd;
    public CompileSettings cSettings;

    public String linkCmd;
    public CompileSettings linkSettings;

    public String filenameExtendtion = "";

    protected BuildableExecutable executable;
    protected CompileChildProcessTask buildTask;
    protected Project project;

    @Override
    public void visit(Project project) {
        this.project = project;
        super.visit(project);
    }

    @Override
    public void visit(BuildableExecutable exe) {
        executable = exe;

        buildTask = new CompileChildProcessTask();
        buildTask.getCompileContext().module = executable;
        buildTask.addModifier(linkSettings);
        buildTask.setName(genTaskName());
        buildTask.setOutput(project.file(new File(genBuildDir(), executable.getName() + filenameExtendtion)));
        buildTask.setDelegate(linkDelegate);
        project.addTask(buildTask);

        project.addDoLast(createResolveDeps(exe, buildTask));


        GnuCompilerVisitor cppVisitor = new GnuCompilerVisitor();
        cppVisitor.compilerCmd = cppCmd;
        cppVisitor.compileSettings = cppSettings;
        cppVisitor.fileFilter = new RegexFileFilter(".*\\.cpp|cc$");
        cppVisitor.project = project;
        cppVisitor.buildTask = buildTask;
        cppVisitor.platform = platform;
        cppVisitor.variant = variant;
        cppVisitor.extra = "exe";
        cppVisitor.visit(executable);


        GnuCompilerVisitor cVisitor = new GnuCompilerVisitor();
        cVisitor.compilerCmd = cCmd;
        cVisitor.compileSettings = cSettings;
        cVisitor.fileFilter = new RegexFileFilter(".*\\.c$");
        cVisitor.project = project;
        cVisitor.buildTask = buildTask;
        cVisitor.platform = platform;
        cVisitor.variant = variant;
        cVisitor.extra = "exe";
        cVisitor.visit(executable);

    }

    private Runnable createResolveDeps(final BuildableModule m, final CompileChildProcessTask buildTask) {
        return new Runnable() {
            @Override
            public void run() {
                for(Library childLib : m.getDependencies()) {
                    Module module = project.resolveLibrary(childLib, platform);
                    if(module == null) {
                        LOGGER.error("can not resolve library: {}", childLib);
                    } else {
                        if(module instanceof BuildableLibrary) {
                            //TODO: add graph dependency
                        } else if(module instanceof PrecompiledLibrary) {
                            PrecompiledLibrary childPrecompiledLib = (PrecompiledLibrary) module;
                            buildTask.addModifier(createDepLibModifier(childPrecompiledLib));
                        }
                    }
                }
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


    @Override
    public void visit(BuildableLibrary lib) {
    }

    private String genTaskName() {
        return "exe"
                + StringUtils.capitalize(executable.getName())
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
            cmdline.addAll(compileContext.flags);

            cmdline.add("-o");
            cmdline.add(task.getOutput().getSingleFile().getAbsoluteFile().toString());

            for(File input : task.getInput()) {
                cmdline.add(input.getAbsolutePath().toString());
            }

            for(Library l : compileContext.sharedLinkedLibs) {
                cmdline.add("-l" + l.getName());
            }

            return cmdline.toArray(new String[cmdline.size()]);
        }

        @Override
        public File getWorkingDir(CompileChildProcessTask task) {
            return null;
        }

        @Override
        public void updateEnv(CompileChildProcessTask task, Map<String, String> env) {
            CompileContext compileContext = task.getCompileContext();
            StringContext strEnv = new StringContext();
            strEnv.putAll(env);

            for(Map.Entry<String, String> entry : compileContext.env.entrySet()) {
                final String key = entry.getKey();
                CharSequence value = entry.getValue();

                value = strEnv.resolve(value);
                env.put(key, value.toString());
                strEnv.setVar(key, value);
            }
        }
    };
}
