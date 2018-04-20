package com.devsmart.zookeeper.plugins;

import com.devsmart.zookeeper.projectmodel.BuildableModule;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashSet;

public class CompileContext {

    public final LinkedHashSet<File> includes = new LinkedHashSet<File>();
    public final ArrayList<String> flags = new ArrayList<String>();
    public BuildableModule module;
}
