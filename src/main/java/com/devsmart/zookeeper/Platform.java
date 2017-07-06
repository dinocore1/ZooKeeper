package com.devsmart.zookeeper;


public class Platform {

    public enum OS {
        linux,
        osx,
        ios,
        win,
        android,
        UNKNOWN;
    }

    public enum ARCH {
        x86,
        x86_64,
        mips,
        arm,
        avr,
        UNKNOWN;
    }

    public final OS os;
    public final ARCH arch;

    public static Platform parse(String platformStr) {
        return null;
    }

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
