package com.devsmart.zookeeper;


import java.io.File;
import java.util.LinkedHashSet;

public class CMakeBuildContext extends BuildContext {

    public static class ExternalLibrary {

        public final Library library;

        public ExternalLibrary(Library library) {
            this.library = library;
        }

        @Override
        public boolean equals(Object o) {
            if(o == null || o.getClass() != getClass()) {
                return false;
            }

            ExternalLibrary other = (ExternalLibrary) o;
            return library.equals(other.library);
        }

        @Override
        public int hashCode() {
            return library.hashCode();
        }
    }

    public final LinkedHashSet<ExternalLibrary> mExternalLibDependencies = new LinkedHashSet<ExternalLibrary>();
    public final LinkedHashSet<String> cMakeArgs = new LinkedHashSet<String>();

    public CMakeBuildContext(ZooKeeper zooKeeper, Library library, Platform platform) {
        super(zooKeeper, library, platform);
    }
}
