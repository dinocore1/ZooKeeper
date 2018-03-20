package com.devsmart.zookeeper;

import java.io.File;
import java.util.List;

interface CompileTarget {
    File getInput();
    File getOutput();
    List<File> getIncludes();
    List<String> getFlags();
        
}