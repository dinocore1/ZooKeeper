package com.devsmart.zookeeper;


import com.google.gson.JsonObject;

public class CompilerConfig {


    String name;
    String language;
    String workingDir;
    String compileCmd;
    String compileFlags;
    String linkerCmd;
    String linkerFlags;
    String targetPlatform;


    JsonObject debug;
    JsonObject release;

}
