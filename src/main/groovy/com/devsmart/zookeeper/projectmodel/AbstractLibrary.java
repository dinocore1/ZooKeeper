package com.devsmart.zookeeper.projectmodel;

import com.devsmart.zookeeper.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AbstractLibrary implements Library {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractLibrary.class);
    private static final Pattern REGEX_STRING = Pattern.compile("([a-zA-Z0-9]+):([a-zA-Z0-9\\\\.\\\\-]+)");

    private String mName;
    private Version mVersion;

    public static AbstractLibrary parse(String string) {
        Matcher m = REGEX_STRING.matcher(string);
        if(m.find()) {
            AbstractLibrary retval = new AbstractLibrary();
            retval.mName = m.group(1);
            retval.mVersion = Version.fromString(m.group(2));
            return retval;
        } else {
            LOGGER.error("could not parse library: {}", string);
            return null;
        }
    }

    @Override
    public String getName() {
        return mName;
    }

    @Override
    public Version getVersion() {
        return mVersion;
    }

    @Override
    public Set<Library> getDependencies() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int hashCode() {
        return mName.hashCode() ^ mVersion.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if(o == null || !(o instanceof AbstractLibrary)) {
            return false;
        }

        return mName.equals(((AbstractLibrary) o).mName)
                && mVersion.equals(((AbstractLibrary) o).mVersion);
    }

    @Override
    public String toString() {
        return String.format("%s:%s", mName, mVersion);
    }
}
