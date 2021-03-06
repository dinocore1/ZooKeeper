package com.devsmart.zookeeper

import com.devsmart.zookeeper.artifacts.Artifact
import com.devsmart.zookeeper.artifacts.FileArtifact
import com.devsmart.zookeeper.file.FileUtils
import com.devsmart.zookeeper.projectmodel.ProjectVisitor
import com.devsmart.zookeeper.tasks.BuildExeTask
import com.devsmart.zookeeper.tasks.BuildLibTask
import com.devsmart.zookeeper.tasks.BuildTask
import com.devsmart.zookeeper.tasks.BasicTask
import com.devsmart.zookeeper.tasks.CleanTask
import com.devsmart.zookeeper.tasks.ListBuildTasks
import com.devsmart.zookeeper.tasks.MkdirBuildTask
import com.google.common.base.Charsets
import com.google.common.hash.HashFunction
import com.google.common.hash.Hasher
import com.google.common.hash.Hashing
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
    final Queue<Runnable> doLast = new LinkedList<Runnable>()
    final Map<Artifact, BuildTask> artifactMap = [:]
    final Map<TemplateKey, CompileTemplate> templates = [:]

    final List<ProjectVisitor> projectVisitors = []

    File getRootDir() {
        File zookeeperRoot = new File(System.getProperty('user.home'))
        zookeeperRoot = new File(zookeeperRoot, '.zookeeper')
        return zookeeperRoot
    }

    void init(Project project) {
        File zookeeperRoot = getRootDir()

        if(!zookeeperRoot.exists()) {
            firstTimeInstall(zookeeperRoot)
        }


        //read project visitors
        File pluginDir = new File(zookeeperRoot, 'plugins')
        for(File f : pluginDir.listFiles()) {
            CompilerConfiguration cc = new CompilerConfiguration()
            //cc.scriptBaseClass = 'com.devsmart.zookeeper.ZooKeeper_DSL'
            Binding binding = new Binding()
            binding.setProperty("project", project)
            GroovyShell shell = new GroovyShell(binding, cc)

            Script script = shell.parse(f)
            script.run()
        }

        //read pre-compiled libs
        readPrecompiledLib(new File(zookeeperRoot, "install"), project)


        //read template files

        File templateDir = new File(zookeeperRoot, 'templates')
        for(File f : templateDir.listFiles()) {
            CompilerConfiguration cc = new CompilerConfiguration()
            cc.scriptBaseClass = 'com.devsmart.zookeeper.ZooKeeper_DSL'
            Binding binding = new Binding()
            binding.setProperty("project", project)
            GroovyShell shell = new GroovyShell(binding, cc)

            Script script = shell.parse(f)
            script.run()
        }
    }

    void firstTimeInstall(File zookeeperRootDir) {
        LOGGER.info("running first time install")
        zookeeperRootDir.mkdirs()

        File pluginDir = new File(zookeeperRootDir, "plugins")
        pluginDir.mkdirs()

    }

    private readPrecompiledLib(File f, Project project) {
        if(f.isFile() && 'lib.zoo'.equals(f.name)) {

            Project childProj = new Project(f.getParentFile(), this);

            LOGGER.info("reading: {}", f.absolutePath)
            CompilerConfiguration cc = new CompilerConfiguration()
            cc.scriptBaseClass = 'com.devsmart.zookeeper.ZooKeeper_DSL'
            Binding binding = new Binding()
            binding.setProperty("project", childProj)
            GroovyShell shell = new GroovyShell(binding, cc)

            Script script = shell.parse(f)
            script.run()

            project.modules.addAll(childProj.modules)

        } else if(f.isDirectory()) {
            for(File fd : f.listFiles()) {
                readPrecompiledLib(fd, project)
            }
        }

    }

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

    private static final HashFunction HASH_FUNCTION = Hashing.sha1()

    private static String createArtifactFileName(String buildName, Version version, File srcFile) {

        Hasher hasher = HASH_FUNCTION.newHasher()
        hasher.putString(buildName, Charsets.UTF_8)
        hasher.putString(version.toString(), Charsets.UTF_8)
        hasher.putString(srcFile.name, Charsets.UTF_8)

        String hashStr = hasher.hash().toString().substring(0, 5)

        String newName = srcFile.name + '_' + hashStr + '.o'
        return newName
    }

    void runDoLast() {
        Runnable r
        while( (r = doLast.poll()) != null) {
            r.run()
        }
    }

    static void main(String[] args) {

        ZooKeeper zooKeeper = new ZooKeeper()

        File projectDir = new File(".").getCanonicalFile()
        Project project = new Project(projectDir, zooKeeper)

        zooKeeper.init(project)


        zooKeeper.dependencyGraph.addTask(new ListBuildTasks(zooKeeper), 'tasks')
        zooKeeper.dependencyGraph.addTask(new CleanTask(zooKeeper), 'clean')

        Options options = new Options()
        options.addOption(Option.builder("i")
            .hasArg()
            .argName('input .zoo file')
            .desc('the input ZOO file')
            .build())

        options.addOption(Option.builder("j")
            .hasArg()
            .argName('num concurrent jobs')
            .desc('number of concurrent jobs')
            .build())

        CommandLineParser parser = new DefaultParser()

        try {
            CommandLine cmdline = parser.parse(options, args)

            String inputFileStr = cmdline.getOptionValue('i', 'build.zoo')
            File inputFile = new File(inputFileStr)
            if(inputFile.exists() && inputFile.isFile()) {
                LOGGER.info("reading: {}", inputFile.getAbsolutePath())
                CompilerConfiguration cc = new CompilerConfiguration()
                cc.scriptBaseClass = 'com.devsmart.zookeeper.ZooKeeper_DSL'
                Binding binding = new Binding()
                binding.setProperty("project", project)
                GroovyShell shell = new GroovyShell(binding, cc)

                Script script = shell.parse(inputFile)
                script.run()

                project.visit(zooKeeper.projectVisitors)

                project.addTaskAlias()

                zooKeeper.runDoLast()


            } else {
                System.err.println("could not open file: ${inputFile.absolutePath}")
                System.exit(1)
            }

            int numJobs = Integer.parseInt(cmdline.getOptionValue('j', '1'))

            String[] unparsedArgs = cmdline.getArgs()
            boolean success = project.build(numJobs, unparsedArgs)
            if(success) {
                System.out.println("Build success")
                System.exit(0)
            } else {
                System.out.println("Build failure")
                System.exit(1)
            }


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
