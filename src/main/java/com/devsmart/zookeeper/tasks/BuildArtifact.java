package com.devsmart.zookeeper.tasks;


import java.io.File;
import java.util.ArrayList;

public abstract class BuildArtifact implements BuildTask {

    public final ArrayList<File> inputFiles = new ArrayList<File>();
    public final ArrayList<File> outputFiles = new ArrayList<File>();

}
