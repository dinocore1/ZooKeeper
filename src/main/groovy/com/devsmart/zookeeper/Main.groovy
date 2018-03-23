package com.devsmart.zookeeper

import com.google.common.io.Resources
import org.codehaus.groovy.control.CompilerConfiguration

class Main {



    static void main(String[] args) {
        try {

            ArrayList<CompileTemplateBuilder> templates = new ArrayList<>();
            Binding binding = new Binding();
            binding.setVariable("templates", templates);


            CompilerConfiguration cc = new CompilerConfiguration()
            cc.scriptBaseClass = 'com.devsmart.zookeeper.ZooKeeperDSL'

            GroovyShell shell = new GroovyShell(ZooKeeperDSL.class.classLoader, binding, cc);
            Object result = shell.evaluate(Resources.getResource("test.txt").toURI())

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
