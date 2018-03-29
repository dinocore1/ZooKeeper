package com.devsmart.zookeeper

import com.devsmart.zookeeper.artifacts.Artifact
import com.devsmart.zookeeper.artifacts.FileArtifact
import com.devsmart.zookeeper.artifacts.PhonyArtifact
import com.devsmart.zookeeper.tasks.BuildExeTask
import com.devsmart.zookeeper.tasks.BuildTask
import com.devsmart.zookeeper.tasks.BasicTask
import com.devsmart.zookeeper.tasks.ListBuildTasks
import com.devsmart.zookeeper.tasks.MkdirBuildTask
import org.apache.commons.cli.CommandLine
import org.apache.commons.cli.CommandLineParser
import org.apache.commons.cli.DefaultParser
import org.apache.commons.cli.HelpFormatter
import org.apache.commons.cli.Option
import org.apache.commons.cli.Options
import org.codehaus.groovy.control.CompilerConfiguration
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.text.ParseException

class ZooKeeper {

    private static final Logger LOGGER = LoggerFactory.getLogger(ZooKeeper.class)

    public final DependencyGraph dependencyGraph = new DependencyGraph()
    private final Queue<Runnable> mDoLast = new LinkedList<Runnable>()
    private final Map<Artifact, BuildTask> mArtifactMap = [:]
    CompileTemplate compileTemplate
    CompileTemplate linkTemplate


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

        File exeFile = new File(buildDir, t.name)

        BuildTask mkdirTask = new MkdirBuildTask(buildDir)
        dependencyGraph.addTask(mkdirTask)

        BasicTask linkTask = new BasicTask()
        linkTask.name = StringUtils.toCamelCase("link", t.name, variant)
        linkTask.output = FileUtils.from(exeFile)
        addTask(linkTask)
        dependencyGraph.addDependency(t, linkTask)
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
            compileTask.input = FileUtils.from(f)

            File outputFile = new File(buildDir, createArtifactFileName(existingOutputFiles, f))
            objFiles.add(outputFile)
            compileTask.output = FileUtils.from(outputFile)

            Closure code
            ApplyTemplate ctx = new ApplyTemplate()
            ctx.input = compileTask.input
            ctx.output = compileTask.output

            code = compileTemplate.all.rehydrate(ctx, null, null)
            code.resolveStrategy = Closure.DELEGATE_ONLY
            code()

            code = compileTemplate.debug.rehydrate(ctx, null, null)
            code.resolveStrategy = Closure.DELEGATE_ONLY
            code()


            code = compileTemplate.cmd.rehydrate(ctx, null, null)
            code.resolveStrategy = Closure.DELEGATE_ONLY
            compileTask.cmd = code

            addTask(compileTask)
            dependencyGraph.addDependency(compileTask, mkdirTask)
            dependencyGraph.addDependency(linkTask, compileTask)

        }

        linkTask.input = FileUtils.from(objFiles)
        ApplyTemplate ctx = new ApplyTemplate()
        ctx.input = linkTask.input
        ctx.output = linkTask.output
        Closure code = linkTemplate.cmd.rehydrate(ctx, null, null)
        code.resolveStrategy = Closure.DELEGATE_ONLY
        linkTask.cmd = code

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

    void build(String... taskNames) {
        for(String taskName : taskNames) {
            BuildTask buildTask = dependencyGraph.getTask(taskName)
            if(buildTask != null) {
                ExePlan plan = dependencyGraph.createExePlan(buildTask)
                plan.run(4)
            } else {
                LOGGER.warn('no task with name: {}', taskName)
            }
        }
    }

    static void main(String[] args) {

        ZooKeeper zooKeeper = new ZooKeeper()

        zooKeeper.dependencyGraph.addTask(new ListBuildTasks(zooKeeper), "tasks")



        Options options = new Options()
        options.addOption(Option.builder("i")
            .hasArg()
            .argName('input .zoo file')
            .desc('the input ZOO file')
            .build())

        CommandLineParser parser = new DefaultParser()

        try {
            CommandLine cmdline = parser.parse(options, args)

            String inputFileStr = cmdline.getOptionValue('i', 'build.zoo')
            File inputFile = new File(inputFileStr)
            if(inputFile.exists() && inputFile.isFile()) {
                CompilerConfiguration cc = new CompilerConfiguration()
                cc.scriptBaseClass = 'com.devsmart.zookeeper.ZooKeeper_DSL'
                Binding binding = new Binding()
                binding.setProperty("zooKeeper", zooKeeper)
                GroovyShell shell = new GroovyShell(binding, cc)

                Script script = shell.parse(inputFile)
                script.run()

                zooKeeper.runDoLast()


            } else {
                System.err.println("could not open file: ${inputFile.absolutePath}")
                System.exit(1)
            }

            String[] unparsedArgs = cmdline.getArgs()
            zooKeeper.build(unparsedArgs)


        } catch (ParseException e) {
            System.err.println("cmd line parse failed: " + e.getMessage())
            HelpFormatter formatter = new HelpFormatter()
            formatter.printHelp("zookeeper [OPTIONS] [target]...", options)
            System.exit(1)
        } catch (Throwable e) {
            e.printStackTrace()
            System.exit(1)
        }

        System.exit(0)

    }

}
