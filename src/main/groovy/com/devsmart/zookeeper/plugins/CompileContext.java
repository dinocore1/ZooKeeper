package com.devsmart.zookeeper.plugins;

import com.devsmart.zookeeper.projectmodel.AbstractLibrary;
import com.devsmart.zookeeper.projectmodel.BuildableModule;

import java.io.File;
import java.util.LinkedHashSet;

public class CompileContext {

    public final LinkedHashSet<File> includes = new LinkedHashSet<>();
    public final LinkedHashSet<String> flags = new LinkedHashSet<>();
    public final LinkedHashSet<AbstractLibrary> staticLinkedLibs = new LinkedHashSet<>();
    public final LinkedHashSet<AbstractLibrary> sharedLinkedLibs = new LinkedHashSet<>();
    public BuildableModule module;
}
