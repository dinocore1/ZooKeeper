package com.devsmart.zookeeper

import com.devsmart.zookeeper.api.FileCollection
import com.devsmart.zookeeper.api.FileTree
import com.devsmart.zookeeper.artifacts.FileArtifact
import com.devsmart.zookeeper.artifacts.PhonyArtifact
import com.devsmart.zookeeper.file.DefaultBaseDirFileResolver
import com.devsmart.zookeeper.file.DefaultFileCollection
import com.devsmart.zookeeper.file.DefaultFileTree
import com.devsmart.zookeeper.projectmodel.BuildableExecutable
import com.devsmart.zookeeper.projectmodel.BuildableLibrary
import com.devsmart.zookeeper.projectmodel.BuildableModule
import com.devsmart.zookeeper.projectmodel.Library
import com.devsmart.zookeeper.projectmodel.Module
import com.devsmart.zookeeper.projectmodel.PrecompiledLibrary
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
    public final List<Module> modules = new ArrayList<>()
    final List<BuildTask> exeTasks = []
    final List<BuildTask> libTasks = []

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

    CompileChildProcessTask getExe(String name) {


        return exeTasks.find { it ->
            BuildTask exe = it

            if(exe instanceof CompileChildProcessTask) {
                CompileChildProcessTask compileChildProcessTask = exe
                if( Platform.nativePlatform.equals( compileChildProcessTask.compileContext.platform )
                 && exe.compileContext.module.name.equals (name)){
                    return true
                }
            }

            return false
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

    void addExeBuildTask(BuildTask t) {
        exeTasks.add(t)
        addTask(t)
    }

    void addLibBuildTask(BuildTask t) {
        libTasks.add(t)
        addTask(t)
    }

    private Runnable resolveDependencies(BasicTask t) {
        return {

            if(t instanceof BuildTask) {

                for (File inputFile : t.input) {
                    BuildTask childTask = zooKeeper.artifactMap.get(new FileArtifact(inputFile))
                    if (childTask != null) {
                        zooKeeper.dependencyGraph.addDependency((BuildTask) t, childTask)
                    }
                }

                for (Object depend : t.dependencies) {
                    if (depend instanceof String) {
                        BuildTask childTask

                        childTask = zooKeeper.dependencyGraph.getTask((String) depend)
                        if(childTask == null) {
                            childTask = zooKeeper.artifactMap.get(new PhonyArtifact(depend))
                        }

                        if (childTask != null) {
                            zooKeeper.dependencyGraph.addDependency((BuildTask) t, childTask)
                        } else {
                            LOGGER.warn("could not resolve task: {}", depend)
                        }
                    } else if(depend instanceof BuildTask) {
                        zooKeeper.dependencyGraph.addDependency((BuildTask) t, depend)
                    }
                }

            }

        }
    }


    void addTaskAlias() {
        BuildTask phonyBuildTask = new BuildTask() {
            @Override
            boolean run(){}
        }
        PhonyArtifact buildArtifact = new PhonyArtifact('build')

        zooKeeper.dependencyGraph.addTask(phonyBuildTask, 'build')
        zooKeeper.artifactMap.put(buildArtifact, phonyBuildTask)

        for(BuildTask exe : exeTasks) {
            zooKeeper.dependencyGraph.addDependency(phonyBuildTask, exe)
        }

        for(BuildTask lib : libTasks) {
            zooKeeper.dependencyGraph.addDependency(phonyBuildTask, lib)
        }

        for(BuildTask exe : exeTasks) {
            if(exe instanceof CompileChildProcessTask) {
                CompileChildProcessTask compileChildProcessTask = exe
                if( Platform.nativePlatform.equals( compileChildProcessTask.compileContext.platform )){
                    PhonyArtifact exeBuildArtifact = new PhonyArtifact(compileChildProcessTask.compileContext.module.name)
                    zooKeeper.artifactMap.put(exeBuildArtifact, exe)
                }
            }
        }


    }

    synchronized Module resolveLibrary(Library library, Platform platform) {
        ArrayList<Library> bestList = new ArrayList<Library>()

        for(Module m : modules) {
            if(m.name.equals(library.name) && m.version.compareTo(library.version) >= 0) {
                if(m instanceof PrecompiledLibrary && m.platform.equals(platform)) {
                    bestList.add(m)
                } else if(m instanceof BuildableLibrary) {
                    bestList.add(m)
                }
            }
        }

        bestList.sort()
        if(bestList.isEmpty()) {
            return null
        } else {
            return (Module) bestList.last()
        }
    }

    private class CompileContext {
        GenericBuildTask parentTask
        Platform target
        String variant
        File buildDir
        final List<File> includeDirs = []
        final List<File> objectFiles = []
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
                retval = false
            }
        }

        return retval
    }


}
