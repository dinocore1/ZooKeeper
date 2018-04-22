package com.devsmart.zookeeper.plugins;

import com.devsmart.zookeeper.StringContext;
import com.devsmart.zookeeper.projectmodel.BuildableLibrary;
import com.devsmart.zookeeper.projectmodel.Library;
import com.devsmart.zookeeper.projectmodel.Module;
import com.devsmart.zookeeper.projectmodel.PrecompiledLibrary;
import com.devsmart.zookeeper.tasks.CompileChildProcessTask;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Map;

public class GnuCompilerVisitor extends BasicCompilerFileVisitor {


    String compilerCmd;
    CompileProcessModifier compileSettings;
    FileFilter fileFilter;

    protected BuildableLibrary mLibrary;
    protected CompileProcessModifier mProjectModifier;
    protected CompileProcessModifier mDependenciesModifier;


    @Override
    public void visit(BuildableLibrary lib) {
        this.mLibrary = lib;
        createProjectModifier();
        createDependencyModifier();
        super.visit(lib);
    }

    @Override
    public void visit(File srcFile) {
        if(fileFilter.accept(srcFile)) {
            super.visit(srcFile);
            compileTask.addModifier(compileSettings);
            compileTask.addModifier(mProjectModifier);
            compileTask.addModifier(mDependenciesModifier);
            compileTask.setDelegate(compileDelegate);
        }
    }

    private void createProjectModifier() {
        mProjectModifier = new CompileProcessModifier() {

            @Override
            public void apply(CompileChildProcessTask ctx) {
                ctx.getCompileContext().macrodefines.addAll(mLibrary.getMacrodefs());
                ctx.getCompileContext().includes.addAll(mLibrary.getIncludes().getFiles());
            }
        };
    }

    private void createDependencyModifier() {
        mDependenciesModifier = new CompileProcessModifier() {
            @Override
            public void apply(CompileChildProcessTask ctx) {
                CompileSettings settings = new CompileSettings();

                for(Library lib : mLibrary.getDependencies()) {
                    Module module = project.resolveLibrary(lib, platform);

                    if(module instanceof PrecompiledLibrary) {
                        PrecompiledLibrary precompiledLibrary = (PrecompiledLibrary) module;

                    }


                }

                ctx.getCompileContext().flags.addAll(settings.getFlags());
                ctx.getCompileContext().includes.addAll(settings.getIncludes());
                ctx.getCompileContext().sharedLinkedLibs.addAll(settings.getSharedLinkedLibs());
                ctx.getCompileContext().staticLinkedLibs.addAll(settings.getStaticLinkedLibs());

            }
        };
    }

    private CompileChildProcessTask.Delegate compileDelegate = new CompileChildProcessTask.Delegate() {

        @Override
        public String[] getCommandLine(CompileChildProcessTask task) {
            task.doModify();
            CompileContext compileContext = task.getCompileContext();
            ArrayList<String> cmdline = new ArrayList<>();
            cmdline.add(compilerCmd);
            cmdline.addAll(compileContext.flags);
            cmdline.add("-c");

            for(String def : compileContext.macrodefines) {
                cmdline.add("-D" + def);
            }

            for(File includeDir : compileContext.includes) {
                cmdline.add("-I" + includeDir.getAbsoluteFile().toString());
            }

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
            CompileContext compileContext = task.getCompileContext();
            StringContext strEnv = new StringContext();
            strEnv.putAll(env);

            for(Map.Entry<String, String> entry : compileContext.env.entrySet()) {
                final String key = entry.getKey();
                String value = entry.getValue();

                value = strEnv.resolve(value);
                env.put(key, value);
                strEnv.setVar(key, value);
            }
        }
    };
}
