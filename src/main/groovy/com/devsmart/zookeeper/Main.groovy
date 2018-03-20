package com.devsmart.zookeeper

import com.google.common.io.Resources
import org.codehaus.groovy.control.CompilerConfiguration

class Main {



    static void main(String[] args) {
        try {
            List<String> classpath = new ArrayList<>();
            classpath.add("com.devsmart.zookeeper");
            CompilerConfiguration cc = new CompilerConfiguration();
            //cc.setScriptBaseClass( Main.class.getName() );
            cc.setClasspathList(classpath);



            ArrayList<CompileTemplateBuilder> templates = new ArrayList<>();

            Binding binding = new Binding();
            binding.setVariable("templates", templates);

            GroovyShell shell = new GroovyShell(Main.class.getClassLoader(), binding, cc);


            Script script = shell.parse(Resources.getResource("test.txt").toURI());


            Object result = script.run();


            CompileTemplate template = templates.get(0).getDebugVariant();

            result = template.getCmdLine(new CompileTarget() {
                @Override
                File getInput() {
                    return new File("input")
                }

                @Override
                File getOutput() {
                    return new File("output")
                }

                @Override
                List<File> getIncludes() {
                    return [ new File("include1"), new File("include2") ]
                }

                @Override
                List<String> getFlags() {
                    return []
                }
            })

            System.out.println()
            System.out.println("result: " + result)



        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
