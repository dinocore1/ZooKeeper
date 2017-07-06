package com.devsmart.zookeeper;


import java.io.File;
import java.util.LinkedHashSet;

public class CMakeBuildContext extends BuildContext {

    public static class ExternalLibrary {

        public final Library library;
        public final File cmakeExportDir;

        public ExternalLibrary(Library library, File cmakeExportDir) {
            this.library = library;
            this.cmakeExportDir = cmakeExportDir;
        }

        @Override
        public boolean equals(Object o) {
            if(o == null || o.getClass() != getClass()) {
                return false;
            }

            ExternalLibrary other = (ExternalLibrary) o;
            return library.equals(other.library) && cmakeExportDir.equals(other.cmakeExportDir);
        }

        @Override
        public int hashCode() {
            return library.hashCode() ^ cmakeExportDir.hashCode();
        }
    }

    public final LinkedHashSet<ExternalLibrary> mExternalLibDependencies = new LinkedHashSet<ExternalLibrary>();
    public final LinkedHashSet<String> cMakeArgs = new LinkedHashSet<String>();

    public CMakeBuildContext(Library library, Platform platform) {
        super(library, platform);
    }
}
