package com.devsmart.zookeeper.plugins;

import com.devsmart.zookeeper.DefaultLibrary;
import com.devsmart.zookeeper.projectmodel.BuildableModule;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;

public class CompileContext {

    public final LinkedHashSet<File> includes = new LinkedHashSet<>();
    public final LinkedHashSet<String> flags = new LinkedHashSet<>();
    public final LinkedHashSet<DefaultLibrary> staticLinkedLibs = new LinkedHashSet<>();
    public final LinkedHashSet<DefaultLibrary> sharedLinkedLibs = new LinkedHashSet<>();
    public final LinkedHashSet<String> macrodefines = new LinkedHashSet<>();
    public final LinkedHashMap<String, String> env = new LinkedHashMap<>();
    public BuildableModule module;
}
