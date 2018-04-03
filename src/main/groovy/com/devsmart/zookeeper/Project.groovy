package com.devsmart.zookeeper

import com.devsmart.zookeeper.api.FileCollection
import com.devsmart.zookeeper.api.FileTree
import com.devsmart.zookeeper.artifacts.FileArtifact
import com.devsmart.zookeeper.file.DefaultBaseDirFileResolver
import com.devsmart.zookeeper.file.DefaultFileCollection
import com.devsmart.zookeeper.file.DefaultFileTree
import com.devsmart.zookeeper.tasks.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class Project {

    private static final Logger LOGGER = LoggerFactory.getLogger(Project.class)

    private ZooKeeper mZooKeeper
    private File mProjectDir
    private DefaultBaseDirFileResolver mBaseDirFileResolver

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

    void addDoLast(Runnable r) {
        zooKeeper.doLast.add(r)
    }

    BuildExeTask getExe(String name) {
        return zooKeeper.exeTasks.find { it ->
            it.name.equals(name)
        }
    }

    void addTask(BasicTask t) {
        String taskName = t.name
        if(taskName != null) {
            zooKeeper.dependencyGraph.addTask(t, taskName)
        } else {
            zooKeeper.dependencyGraph.addTask(t)
        }

        for(File f : t.output) {
            zooKeeper.artifactMap.put(new FileArtifact(f), t)
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

    private void buildExeTasks(BuildExeTask t) {
        Platform platform = Platform.getNativePlatform()
        String variant = "debug"

        File buildDir = new File(projectDir, 'build')
        buildDir = new File(buildDir, platform.toString())
        buildDir = new File(buildDir, variant)

        if(t.includes == null) {
            t.includes = files('src', 'include')
        }
        Set<File> includeDirs = t.getIncludes().files

        File exeFile = new File(buildDir, t.name)

        BuildTask mkdirTask = new MkdirBuildTask(buildDir)
        zooKeeper.dependencyGraph.addTask(mkdirTask)

        t.output = file(exeFile)
        List<File> objFiles = []

        for(File f : t.sources) {
            if(!f.exists()) {
                FileArtifact artifactKey = new FileArtifact(f)
                BuildTask parentBuildTask = mArtifactMap.get(artifactKey)
                if(parentBuildTask == null) {
                    LOGGER.error("no build definition for: {}", artifactKey)
                } else {
                    dependencyGraph.addDependency(t, parentBuildTask)
                }
            }

            BasicTask compileTask = new BasicTask()
            compileTask.input = file(f)

            File outputFile = new File(buildDir, ZooKeeper.createArtifactFileName(t.name, t.version, f))
            objFiles.add(outputFile)
            compileTask.output = file(outputFile)

            Closure code
            ApplyTemplate ctx = new ApplyTemplate()
            ctx.input = compileTask.input
            ctx.output = compileTask.output
            ctx.includes.addAll(includeDirs)

            code = zooKeeper.compileTemplate.all.rehydrate(ctx, this, null)
            code.resolveStrategy = Closure.DELEGATE_FIRST
            code()

            code = zooKeeper.compileTemplate.debug.rehydrate(ctx, this, null)
            code.resolveStrategy = Closure.DELEGATE_FIRST
            code()

            code = zooKeeper.compileTemplate.cmd.rehydrate(ctx, this, null)
            code.resolveStrategy = Closure.DELEGATE_FIRST
            compileTask.cmd = code

            addTask(compileTask)
            zooKeeper.dependencyGraph.addDependency(compileTask, mkdirTask)
            zooKeeper.dependencyGraph.addDependency(t, compileTask)

        }

        t.input = files(objFiles)
        ApplyTemplate ctx = new ApplyTemplate()
        ctx.input = t.input
        ctx.output = t.output
        Closure code = zooKeeper.linkTemplate.cmd.rehydrate(ctx, this, null)
        code.resolveStrategy = Closure.DELEGATE_FIRST
        t.cmd = code

    }

    void buildLibTask(BuildLibTask t) {
        Platform platform = Platform.getNativePlatform()
        String variant = "debug"

        File buildDir = new File(projectDir, 'build')
        buildDir = new File(buildDir, platform.toString())
        buildDir = new File(buildDir, variant)

        if(t.includes == null) {
            t.includes = files('src', 'include')
        }
        Set<File> includeDirs = t.getIncludes().files

        File exeFile = new File(buildDir, t.name  + ".a")

        BuildTask mkdirTask = new MkdirBuildTask(buildDir)
        zooKeeper.dependencyGraph.addTask(mkdirTask)

        t.output = file(exeFile)
        List<File> objFiles = []

        for(File f : t.sources) {
            if(!f.exists()) {
                FileArtifact artifactKey = new FileArtifact(f)
                BuildTask parentBuildTask = zooKeeper.artifactMap.get(artifactKey)
                if(parentBuildTask == null) {
                    LOGGER.error("no build definition for: {}", artifactKey)
                } else {
                    zooKeeper.dependencyGraph.addDependency(t, parentBuildTask)
                }
            }

            BasicTask compileTask = new BasicTask()
            compileTask.input = file(f)

            File outputFile = new File(buildDir, ZooKeeper.createArtifactFileName(t.name, t.version, f))
            objFiles.add(outputFile)
            compileTask.output = file(outputFile)

            Closure code
            ApplyTemplate ctx = new ApplyTemplate()
            ctx.input = compileTask.input
            ctx.output = compileTask.output
            ctx.includes.addAll(includeDirs)

            code = zooKeeper.compileTemplate.all.rehydrate(ctx, this, null)
            code.resolveStrategy = Closure.DELEGATE_FIRST
            code()

            code = zooKeeper.compileTemplate.debug.rehydrate(ctx, this, null)
            code.resolveStrategy = Closure.DELEGATE_FIRST
            code()

            code = zooKeeper.compileTemplate.cmd.rehydrate(ctx, this, null)
            code.resolveStrategy = Closure.DELEGATE_FIRST
            compileTask.cmd = code

            addTask(compileTask)
            zooKeeper.dependencyGraph.addDependency(compileTask, mkdirTask)
            zooKeeper.dependencyGraph.addDependency(t, compileTask)

        }

        t.input = files(objFiles)
        ApplyTemplate ctx = new ApplyTemplate()
        ctx.input = t.input
        ctx.output = t.output
        Closure code = zooKeeper.staticLibTemplate.cmd.rehydrate(ctx, this, null)
        code.resolveStrategy = Closure.DELEGATE_FIRST
        t.cmd = code
    }

    void build(String... taskNames) {
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

                int cores = Runtime.getRuntime().availableProcessors();
                plan.run(cores)
            } else {
                LOGGER.warn('no task with name: {}', taskName)
            }
        }
    }


}
