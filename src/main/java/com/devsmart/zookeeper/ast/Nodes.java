package com.devsmart.zookeeper.ast;


import com.devsmart.zookeeper.Library;
import com.devsmart.zookeeper.Version;
import com.devsmart.zookeeper.ZooKeeperParser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Nodes {

    public static abstract class Node {

    }

    public static class FileNode extends Node {
        public final List<LibNode> libraries = new ArrayList<LibNode>();
    }

    public static class VersionNode extends Node {
        public final Version version = new Version();

    }

    public static class LibNode extends Node {
        public final Library library;

        public final List<Library> compileLibDependencies = new ArrayList<Library>();
        public final List<Library> testLibDependencies = new ArrayList<Library>();

        public LibNode(String name, Version version) {
            library = new Library(name, version);
        }
    }

    public static class KeyValue extends Node {
        private final String mKey;
        private final String mValue;

        public KeyValue(String key, String value) {
            mKey = key;
            mValue = value;
        }
    }
}
