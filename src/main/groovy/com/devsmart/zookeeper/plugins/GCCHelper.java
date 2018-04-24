package com.devsmart.zookeeper.plugins;

import com.devsmart.zookeeper.Platform;
import com.devsmart.zookeeper.Project;
import com.devsmart.zookeeper.projectmodel.*;

import java.io.File;
import java.util.Collection;
import java.util.Set;

public class GCCHelper {

    public static void libraryLinkerLine(Project project, Collection<Library> modules, Platform platform, Set<File> linkerSearchPath, Set<String> librarySet) {
        for(Library dep : modules) {
            libraryLinkerLine(project, dep, platform, linkerSearchPath, librarySet);
        }
    }

    public static void libraryLinkerLine(Project project, Library library, Platform platform, Set<File> linkerSearchPath, Set<String> librarySet) {
        Module childModule = project.resolveLibrary(library, platform);
        if(childModule == null) {
            librarySet.add(library.getName());
        } else {
            if(childModule instanceof PrecompiledLibrary) {
                PrecompiledLibrary precompileLib = (PrecompiledLibrary) childModule;

                Set<File> libFiles = precompileLib.getLib().getFiles();
                if(!libFiles.isEmpty()) {
                    for(File f : libFiles) {
                        linkerSearchPath.add(f.getParentFile());
                    }
                }

                librarySet.add(library.getName());

                libraryLinkerLine(project, precompileLib.getDependencies(), platform, linkerSearchPath, librarySet);


            } else if(childModule instanceof BuildableLibrary){
                BuildableLibrary buildableLibrary = (BuildableLibrary) childModule;
                //TODO: get output dir

                libraryLinkerLine(project, buildableLibrary.getDependencies(), platform, linkerSearchPath, librarySet);
            }
        }
    }
}
