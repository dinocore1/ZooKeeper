package com.devsmart.zookeeper.ast;


import com.devsmart.zookeeper.ZooKeeperParser;
import org.antlr.v4.runtime.ParserRuleContext;

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

    public static class LibNode extends Node {
        public final String mName;
        public final Map<String, String> keyValuePairs = new HashMap<String, String>();
        public final Map<String, ZooKeeperParser.KeyvalueContext> keyValueContext = new HashMap<String, ZooKeeperParser.KeyvalueContext>();

        public LibNode(String name) {
            mName = name;
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
