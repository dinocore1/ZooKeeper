package com.devsmart.zookeeper;


import com.google.common.collect.ComparisonChain;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Version implements Comparable<Version> {

    public int major;
    public int minor;
    public int patch;

    private static final Pattern REGEX = Pattern.compile("^([0-9]+)\\.([0-9]+)\\.([0-9]+)$");

    public static Version fromString(String value) {
        Version retval = null;
        Matcher m = REGEX.matcher(value);
        if(m.find()) {
            retval = new Version();
            retval.major = Integer.parseInt(m.group(1));
            retval.minor = Integer.parseInt(m.group(2));
            retval.patch = Integer.parseInt(m.group(3));
        }

        return retval;
    }

    @Override
    public int hashCode() {
        return (major << 20) & (minor << 10) & patch;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == null) {
            return false;
        }

        if(obj instanceof Version) {
            Version other = (Version) obj;
            return major == other.major && minor == other.minor && patch == other.patch;
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        return String.format("%d.%d.%d", major, minor, patch);
    }

    @Override
    public int compareTo(Version o) {
        return ComparisonChain.start()
                .compare(major, o.major)
                .compare(minor, o.minor)
                .compare(patch, o.patch)
                .result();
    }
}
