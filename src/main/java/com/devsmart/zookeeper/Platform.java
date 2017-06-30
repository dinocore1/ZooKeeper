package com.devsmart.zookeeper;


public class Platform {

    public enum OS {
        LINUX("linux"),
        MACOSX("osx"),
        IOS("ios"),
        WINDOWS("win"),
        ANDROID("android"),
        UNKNOWN("unknown");


        public final String name;

        OS(String name) {
            this.name = name;
        }
    }

    public enum ARCH {
        x86,
        x86_64,
        armv7a,
        UNKNOWN;
    }

    private final OS os;
    private final ARCH arch;

    Platform(OS os, ARCH arch) {
        this.os = os;
        this.arch = arch;
    }

    @Override
    public String toString() {
        return String.format("%s-%s", os, arch);
    }

    @Override
    public int hashCode() {
        return os.hashCode() ^ arch.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == null || obj.getClass() != getClass()) {
            return false;
        }

        Platform other = (Platform) obj;
        return os == other.os && arch == other.arch;
    }
}
