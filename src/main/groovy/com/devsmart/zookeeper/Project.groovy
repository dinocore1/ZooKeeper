package com.devsmart.zookeeper

import com.devsmart.zookeeper.api.FileCollection
import com.devsmart.zookeeper.api.FileTree
import com.devsmart.zookeeper.artifacts.FileArtifact
import com.devsmart.zookeeper.file.DefaultBaseDirFileResolver
import com.devsmart.zookeeper.file.DefaultFileCollection
import com.devsmart.zookeeper.file.DefaultFileTree
import com.devsmart.zookeeper.projectmodel.BuildableExecutable
import com.devsmart.zookeeper.projectmodel.BuildableModule
import com.devsmart.zookeeper.projectmodel.ProjectVisitor
import com.devsmart.zookeeper.tasks.*
import com.google.common.base.Function
import com.google.common.base.Predicate
import com.google.common.collect.Iterables
import com.google.common.collect.Sets
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class Project {

    private static final Logger LOGGER = LoggerFactory.getLogger(Project.class)

    private ZooKeeper mZooKeeper
    private File mProjectDir
    private DefaultBaseDirFileResolver mBaseDirFileResolver
    public final List<BuildableModule> buildableModules = new ArrayList<>()

    Project(File projectDir, ZooKeeper zooKeeper) {
        mProjectDir = projectDir
        mZooKeeper = zooKeeper
        mBaseDirFileResolver = new DefaultBaseDirFileResolver(mProjectDir)
    }

    File getProjectDir() {
        return mProjectDir
    }

    ZooKeeper getZooKeeper() {
        return mZooKeeper
    }


    FileCollection file(Object object) {
        return new DefaultFileCollection(object.toString(), mBaseDirFileResolver, object)
    }

    FileCollection files(Object... objects) {
        return new DefaultFileCollection("file collection", mBaseDirFileResolver, objects)
    }

    FileTree fileTree(Map<String, ?> args) {
        return new DefaultFileTree(args, mBaseDirFileResolver)

    }

    void visit(Collection<ProjectVisitor> visitors) {
        for(ProjectVisitor v : visitors) {
            v.visit(this)
        }
    }

    void addDoLast(Runnable r) {
        zooKeeper.doLast.add(r)
    }

    BuildExeTask getExe(String name) {
        return zooKeeper.exeTasks.find { it ->
            it.name.equals(name)
        }
    }

    void addTask(BuildTask t) {

        zooKeeper.dependencyGraph.addTask(t)

        if (t instanceof BasicTask) {
            String taskName = t.name
            if (taskName != null) {
                zooKeeper.dependencyGraph.setTaskName(t, taskName)

            }

            for (File f : t.output) {
                zooKeeper.artifactMap.put(new FileArtifact(f), (BuildTask)t)
            }

            addDoLast(resolveDependencies(t))
        }

    }

    private Runnable resolveDependencies(BasicTask t) {
        return {

            if(t instanceof BuildTask) {

                for (File inputFile : t.input) {
                    if (!inputFile.exists()) {
                        BuildTask childTask = zooKeeper.artifactMap.get(new FileArtifact(inputFile))
                        if (childTask != null) {
                            zooKeeper.dependencyGraph.addDependency((BuildTask) t, childTask)
                        }
                    }
                }

                for (Object depend : t.dependencies) {
                    if (depend instanceof String) {
                        BuildTask childTask = zooKeeper.dependencyGraph.getTask((String) depend)
                        if (childTask != null) {
                            zooKeeper.dependencyGraph.addDependency((BuildTask) t, childTask)
                        }
                    }
                }

            }

        }
    }

    void addExeTask(BuildExeTask t) {
        addTask(t)
        zooKeeper.exeTasks.add(t)
        zooKeeper.doLast.add({
            buildExeTasks(t)
        })
    }

    void addLibTask(BuildLibTask t) {
        addTask(t)
        zooKeeper.libTasks.add(t)
        zooKeeper.doLast.add({
            buildLibTask(t)
        })
    }

    private class CompileContext {
        GenericBuildTask parentTask
        Platform target
        String variant
        File buildDir
        final List<File> includeDirs = []
        final List<File> objectFiles = []
    }

    private BasicTask createCompileTask(File sourceFile, CompileContext compileCtx) {

        String lang = ''
        if(sourceFile.name.endsWith('.cpp')) {
            lang = 'c++'
        } else if(sourceFile.name.endsWith('.c')) {
            lang = 'c'
        }

        TemplateKey key = new TemplateKey(compileCtx.target, lang, 'compile')
        CompileTemplate template = zooKeeper.templates.get(key)
        if(template == null) {
            LOGGER.warn("could not find template for: {}", key)
            return
        }

        BasicTask compileTask = new BasicTask()
        addTask(compileTask)
        compileTask.input = file(sourceFile)

        if(!sourceFile.exists()) {
            FileArtifact artifactKey = new FileArtifact(sourceFile)
            BuildTask parentBuildTask = zooKeeper.artifactMap.get(artifactKey)
            if(parentBuildTask == null) {
                LOGGER.error("no build definition for: {}", artifactKey)
            } else {
                zooKeeper.dependencyGraph.addDependency(compileTask, parentBuildTask)
            }
        }

        File outputFile = new File(compileCtx.buildDir, ZooKeeper.createArtifactFileName(compileCtx.parentTask.name, compileCtx.parentTask.version, sourceFile))
        compileCtx.objectFiles.add(outputFile)
        compileTask.output = file(outputFile)

        Closure code
        ApplyTemplate ctx = new ApplyTemplate()
        ctx.input = compileTask.input
        ctx.output = compileTask.output
        ctx.includes.addAll(compileCtx.includeDirs)

        code = template.all.rehydrate(ctx, this, null)
        code.resolveStrategy = Closure.DELEGATE_FIRST
        code()

        if('debug'.equals(compileCtx.variant)) {
            code = template.debug.rehydrate(ctx, this, null)
            code.resolveStrategy = Closure.DELEGATE_FIRST
            code()
        } else if('release'.equals(compileCtx.variant)) {
            code = template.release.rehydrate(ctx, this, null)
            code.resolveStrategy = Closure.DELEGATE_FIRST
            code()
        }

        code = template.cmd.rehydrate(ctx, this, null)
        code.resolveStrategy = Closure.DELEGATE_FIRST
        compileTask.cmd = code
        if(template.workingDir != null) {
            compileTask.workingDir = file(template.workingDir).getSingleFile()
        }


        return compileTask
    }

    private Set<Platform> getPlatformList(String stage) {
        Iterable<TemplateKey> matchingStage = Iterables.filter(zooKeeper.templates.keySet(), new Predicate<TemplateKey>(){
            @Override
            boolean apply(TemplateKey input) {
                return stage.equals(input.stage)
            }
        })
        Set<Platform> retval = Sets.newHashSet(Iterables.transform(matchingStage, new Function<TemplateKey, Platform>(){

            @Override
            Platform apply(TemplateKey input) {
                return input.platform
            }
        }))
        return retval
    }

    private void createCompileTasks(GenericBuildTask t, String stage) {

        for(Platform platform : getPlatformList(stage)) {
            for(String variant : ['debug', 'release']) {

                GenericBuildTask buildExeTask = new GenericBuildTask()
                buildExeTask.name = t.name
                buildExeTask.sources = t.sources
                buildExeTask.includes = t.includes
                zooKeeper.dependencyGraph.addTask(buildExeTask, stage + buildExeTask.name.capitalize() + platform.toString().capitalize() + variant.capitalize())

                CompileContext compileCtx = new CompileContext()
                compileCtx.target = platform
                compileCtx.variant = variant
                compileCtx.parentTask = buildExeTask


                File buildDir = new File(projectDir, 'build')
                buildDir = new File(buildDir, compileCtx.target.toString())
                buildDir = new File(buildDir, compileCtx.variant)
                compileCtx.buildDir = buildDir

                if(buildExeTask.includes == null) {
                    buildExeTask.includes = files('src', 'include')
                }
                compileCtx.includeDirs.addAll(buildExeTask.includes.files)

                File exeFile = new File(buildDir, buildExeTask.name)
                BuildTask mkdirTask = new MkdirBuildTask(buildDir)
                zooKeeper.dependencyGraph.addTask(mkdirTask)

                buildExeTask.output = file(exeFile)
                for(File f : buildExeTask.sources) {

                    BasicTask compileTask = createCompileTask(f, compileCtx)

                    zooKeeper.dependencyGraph.addDependency(compileTask, mkdirTask)
                    zooKeeper.dependencyGraph.addDependency(buildExeTask, compileTask)
                }

                TemplateKey linkKey = new TemplateKey(compileCtx.target, 'c', stage)
                CompileTemplate linkTemplate = zooKeeper.templates.get(linkKey)

                buildExeTask.input = files(compileCtx.objectFiles)
                ApplyTemplate ctx = new ApplyTemplate()
                ctx.input = buildExeTask.input
                ctx.output = buildExeTask.output
                Closure code = linkTemplate.cmd.rehydrate(ctx, this, null)
                code.resolveStrategy = Closure.DELEGATE_FIRST
                buildExeTask.cmd = code
                if(linkTemplate.workingDir != null) {
                    buildExeTask.workingDir = file(linkTemplate.workingDir).getSingleFile()
                }
            }
        }
    }

    private void buildExeTasks(BuildExeTask t) {
        createCompileTasks(t, 'link')
    }

    void buildLibTask(BuildLibTask t) {
        createCompileTasks(t, 'staticlib')
        createCompileTasks(t, 'sharedlib')
    }

    boolean build(String... taskNames) {
        boolean retval = true
        for(String taskName : taskNames) {
            BuildTask buildTask = null

            if(buildTask == null) {
                buildTask = zooKeeper.dependencyGraph.getTask(taskName)
            }

            if(buildTask == null) {
                try {
                    File artifactFile = file(taskName).singleFile
                    buildTask = zooKeeper.artifactMap.get(new FileArtifact(artifactFile))
                } catch (Throwable e){}
            }

            if(buildTask != null) {
                ExePlan plan = zooKeeper.dependencyGraph.createExePlan(buildTask)

                int cores = Runtime.getRuntime().availableProcessors()
                retval &= plan.run(cores)
            } else {
                LOGGER.warn('no task with name: {}', taskName)
            }
        }

        return retval
    }


}
