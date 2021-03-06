package com.devsmart.zookeeper;


import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Platform {

    private static final Pattern REGEX = Pattern.compile("(.*)-(.*)");

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
        armv7a,
        avr,
        UNKNOWN;
    }

    public static final Platform WIN64 = Platform.parse("win-x86_64");


    public static String getLibraryExtention(Platform platform) {

        if(platform.os.equalsIgnoreCase(OS.win.name())) {
            return ".dll";
        } else if(platform.os.equalsIgnoreCase(OS.linux.name()) || platform.os.equalsIgnoreCase(OS.android.name())) {
            return ".so";
        } else if(platform.os.equalsIgnoreCase(OS.osx.name())) {
            return ".dylib";
        } else {
            return null;
        }
    }


    public final String os;
    public final String arch;

    public static Platform parse(String platformStr) {
        Platform retval = null;
        Matcher m = REGEX.matcher(platformStr);
        if(m.find()) {
            retval = new Platform(m.group(1), m.group(2));
        }

        return retval;
    }

    Platform(String os, String arch) {
        this.os = os;
        this.arch = arch;
    }

    Platform(OS os, ARCH arch) {
        this(os.name(), arch.name());
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
        return os.equals(other.os) && arch.equals(other.arch);
    }

    public static Platform getNativePlatform() {
        Platform.OS os;
        String OSString = System.getProperty("os.name", "generic").toLowerCase(Locale.ENGLISH);
        if (OSString.contains("mac") || OSString.contains("darwin")) {
            os = Platform.OS.osx;
        } else if (OSString.contains("win")) {
            os = Platform.OS.win;
        } else if (OSString.contains("nux")) {
            os = Platform.OS.linux;
        } else {
            os = Platform.OS.UNKNOWN;
        }

        Platform.ARCH arch;
        String archStr = System.getProperty("os.arch", "generic").toLowerCase(Locale.ENGLISH);
        if(archStr.contains("x86_64") || archStr.contains("amd64")) {
            arch = Platform.ARCH.x86_64;
        } else if(archStr.contains("x86")){
            arch = Platform.ARCH.x86;
        } else {
            arch = Platform.ARCH.UNKNOWN;
        }

        return new Platform(os, arch);
    }
}
