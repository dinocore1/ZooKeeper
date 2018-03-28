package com.devsmart.zookeeper

import com.devsmart.zookeeper.artifacts.Artifact
import com.devsmart.zookeeper.artifacts.FileArtifact
import com.devsmart.zookeeper.artifacts.PhonyArtifact
import com.devsmart.zookeeper.tasks.BuildExeTask
import com.devsmart.zookeeper.tasks.BuildTask
import com.devsmart.zookeeper.tasks.BasicTask
import org.codehaus.groovy.control.CompilerConfiguration
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class ZooKeeper {

    private static final Logger LOGGER = LoggerFactory.getLogger(ZooKeeper.class)

    public final DependencyGraph dependencyGraph = new DependencyGraph()
    private final Queue<Runnable> mDoLast = new LinkedList<Runnable>()
    private final Map<Artifact, BuildTask> mArtifactMap = [:]


    void resolveTaskDependencies(BasicTask t) {
        for(String taskName : t.dependencies) {
            BuildTask childTask = dependencyGraph.getTask(taskName)
            if(childTask == null) {
                LOGGER.warn("could not resolve task: {}", taskName)
            } else {
                dependencyGraph.addDependency(t, childTask)
            }
        }
    }

    void addTask(BasicTask t) {
        String taskName = t.name
        if(taskName != null) {
            dependencyGraph.addTask(t, taskName)
        } else {
            dependencyGraph.addTask(t)
        }

        for(File f : t.output) {
            mArtifactMap.put(new FileArtifact(f), t)
        }

    }

    void addExeTask(BuildExeTask t) {
        addTask(t)
        mDoLast.add({
            buildExeTasks(t)
        })
    }

    private static String createArtifactFileName(Set<String> existingOutputFiles, File srcFile) {
        String newName = srcFile.name + '.o'
        int i = 1;
        while(existingOutputFiles.contains(newName)) {
            newName = String.format("%s%d.o", srcFile.name, i)
        }

        existingOutputFiles.add(newName)
        return newName
    }

    private void buildExeTasks(BuildExeTask t) {
        Set<String> existingOutputFiles = []
        Platform platform = Platform.getNativePlatform()
        String variant = "debug"

        File buildDir = new File("build")
        buildDir = new File(buildDir, platform.toString())
        buildDir = new File(buildDir, variant)

        BasicTask linkTask = new BasicTask()
        linkTask.name = StringUtils.toCamelCase("link", variant, t.name)
        addTask(linkTask)

        dependencyGraph.addDependency(t, linkTask)

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
            compileTask.output = FileUtils.from(new File(buildDir, createArtifactFileName(existingOutputFiles, f)))
            addTask(compileTask)
            dependencyGraph.addDependency(linkTask, compileTask)

        }

    }

    void addDoLast(Runnable r) {
        mDoLast.add(r)
    }

    void runDoLast() {
        Runnable r
        while( (r = mDoLast.poll()) != null) {
            r.run()
        }
    }

    static void main(String[] args) {

        ZooKeeper zooKeeper = new ZooKeeper()

        File buildFile = new File("build.zoo")
        if(buildFile.exists()) {
            CompilerConfiguration cc = new CompilerConfiguration()
            cc.scriptBaseClass = 'com.devsmart.zookeeper.ZooKeeper_DSL'
            Binding binding = new Binding()
            binding.setProperty("zooKeeper", zooKeeper)
            GroovyShell shell = new GroovyShell(binding, cc)

            Script script = shell.parse(buildFile)
            script.run()

            zooKeeper.runDoLast()

        } else {
            LOGGER.error("no build.zoo file found")
            System.exit(-1)
        }

    }

}
