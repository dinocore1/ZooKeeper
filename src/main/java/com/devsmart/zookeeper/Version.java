package com.devsmart.zookeeper;


import com.google.common.collect.ComparisonChain;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Version implements Comparable<Version> {

    public int major;
    public int minor;
    public int revision;

    private static final Pattern REGEX = Pattern.compile("([0-9]+)\\.([0-9]+)\\.([0-9]+)");

    public static Version fromString(String value) {
        Version retval = null;
        Matcher m = REGEX.matcher(value);
        if(m.find()) {
            retval = new Version();
            retval.major = Integer.parseInt(m.group(1));
            retval.minor = Integer.parseInt(m.group(2));
            retval.revision = Integer.parseInt(m.group(3));
        }

        return retval;
    }

    @Override
    public int hashCode() {
        return major ^ minor ^ revision;
    }

    @Override
    public String toString() {
        return String.format("v%d.%d.%d", major, minor, revision);
    }

    @Override
    public int compareTo(Version o) {
        return ComparisonChain.start()
                .compare(major, o.major)
                .compare(minor, o.minor)
                .compare(revision, o.revision)
                .result();
    }
}
