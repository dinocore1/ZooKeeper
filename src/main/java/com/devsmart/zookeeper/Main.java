package com.devsmart.zookeeper;

import com.google.common.base.Joiner;
import com.google.common.io.Resources;
import groovy.lang.Binding;
import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyShell;
import groovy.lang.Script;
import org.codehaus.groovy.control.CompilerConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Main {


    public static void main(String[] args) {

        try {
            List<String> classpath = new ArrayList<>();
            classpath.add("com.devsmart.zookeeper");
            CompilerConfiguration cc = new CompilerConfiguration();
            //cc.setScriptBaseClass( Main.class.getName() );
            cc.setClasspathList(classpath);



            ArrayList<CompileTemplate> templates = new ArrayList<>();

            Binding binding = new Binding();
            binding.setVariable("templates", templates);

            GroovyShell shell = new GroovyShell(Main.class.getClassLoader(), binding, cc);


            Script script = shell.parse(Resources.getResource("test.txt").toURI());


            Object result = script.run();

            System.out.println();
            System.out.println("result: " + result);



        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
