package com.devsmart.zookeeper;

import com.devsmart.zookeeper.projectmodel.Library;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DefaultLibrary extends AbstractLibrary {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultLibrary.class);
    private static final Pattern REGEX_STRING = Pattern.compile("([a-zA-Z0-9]+):([a-zA-Z0-9\\\\.\\\\-]+)");

    public static DefaultLibrary parse(String string) {
        Matcher m = REGEX_STRING.matcher(string);
        if(m.find()) {
            String name = m.group(1);
            Version version = Version.fromString(m.group(2));
            return new DefaultLibrary(name, version);
        } else {
            LOGGER.error("could not parse library: {}", string);
            return null;
        }
    }

    public DefaultLibrary(String name, Version version) {
        this.name = name;
        this.version = version;
    }


    @Override
    public Set<Library> getDependencies() {
        throw new UnsupportedOperationException();
    }

}
