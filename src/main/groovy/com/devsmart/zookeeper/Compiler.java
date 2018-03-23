package com.devsmart.zookeeper;


import java.io.File;

public abstract class Compiler {

    Platform mTargetPlatform;

    public abstract Action createCompileToObjectAction(File inputFile);
}
