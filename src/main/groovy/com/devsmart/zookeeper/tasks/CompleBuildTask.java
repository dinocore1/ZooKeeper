package com.devsmart.zookeeper.tasks;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;

public class CompleBuildTask extends ProcessBuildTask {


    @Override
    public String[] createCommandLine() {


        Binding binding = new Binding();

        GroovyShell shell = new GroovyShell(binding);


        return new String[0];
    }
}
