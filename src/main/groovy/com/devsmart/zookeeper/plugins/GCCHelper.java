package com.devsmart.zookeeper.plugins;

import com.devsmart.zookeeper.Platform;
import com.devsmart.zookeeper.Project;
import com.devsmart.zookeeper.projectmodel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Collection;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GCCHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(GCCHelper.class);

    private static final Pattern[] GCCLIBPATTERS = new Pattern[] {
            Pattern.compile("^lib([a-zA-Z0-9\\-_]+)\\.dll\\.a$"),
            Pattern.compile("^([a-zA-Z0-9\\-_]+)\\.dll\\.a$"),
            Pattern.compile("^lib([a-zA-Z0-9\\-_]+)\\.a$"),
            Pattern.compile("^lib([a-zA-Z0-9\\-_]+)\\.dll$"),
            Pattern.compile("^([a-zA-Z0-9\\-_]+)\\.dll$"),

            Pattern.compile("^lib([a-zA-Z0-9\\-_]+)\\.so$"),
            Pattern.compile("^lib([a-zA-Z0-9\\-_]+)\\.a$"),
            Pattern.compile("^([a-zA-Z0-9\\-_]+)\\.so$"),
            Pattern.compile("^([a-zA-Z0-9\\-_]+)\\.a$"),
    };

    public static String extractGCCLibName(String fileName) {

        for(Pattern pattern : GCCLIBPATTERS) {
            Matcher m = pattern.matcher(fileName);
            if(m.matches()) {
                return m.group(1);
            }
        }
        return null;
    }

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

                        String libName = extractGCCLibName(f.getName());
                        if(libName != null) {
                            librarySet.add(libName);
                        } else {
                            LOGGER.warn("could not extract library name from: {}", f.getAbsolutePath());
                        }
                    }
                }

                libraryLinkerLine(project, precompileLib.getDependencies(), platform, linkerSearchPath, librarySet);


            } else if(childModule instanceof BuildableLibrary){
                BuildableLibrary buildableLibrary = (BuildableLibrary) childModule;
                //TODO: get output dir

                libraryLinkerLine(project, buildableLibrary.getDependencies(), platform, linkerSearchPath, librarySet);
            }
        }
    }
}
