package com.devsmart.zookeeper.ast;


import com.devsmart.zookeeper.*;
import com.google.common.base.Joiner;

import java.util.*;

public class Nodes {

    public static abstract class Node {

    }

    public static String escapeStringLiteral(String input) {
        input = input.substring(1, input.length()-1);
        input = input.replaceAll("\\\\\"", "\"");
        input = input.replace("\\\\", "\\");
        return input;
    }

    public static class VersionNode extends Node {
        public final Version version = new Version();

    }

    public static class LibraryDefNode extends Node {
        public String libName;
        public VersionNode versionNode;
        public ObjectNode objectNode;
    }

    public static class BuildLibraryDefNode extends Node {
        public String libName;
        public VersionNode versionNode;
        public ObjectNode objectNode;
    }

    public static abstract class ValueNode extends Node {
        public boolean isArray() {
            return false;
        }

        public boolean isObject() {
            return false;
        }
    }

    public static class StringNode extends ValueNode {
        public final String value;

        public StringNode(String s) {
            this.value = s;
        }

        @Override
        public String toString() {
            return value;
        }
    }

    public static class ArrayNode extends ValueNode {
        public final ArrayList<ValueNode> array;

        public ArrayNode(ArrayList<ValueNode> values) {
            this.array = values;
        }

        @Override
        public boolean isArray() {
            return true;
        }

        @Override
        public String toString() {
            Joiner joiner = Joiner.on(", ");
            StringBuilder builder = new StringBuilder();
            joiner.appendTo(builder, array);
            return builder.toString();
        }
    }

    public static class ObjectNode extends ValueNode {
        public final KeyValueEntriesNode entries;

        public ObjectNode(KeyValueEntriesNode entries) {
            this.entries = entries;

        }

        @Override
        public boolean isObject() {
            return true;
        }
    }

    public static class KeyValueEntriesNode extends Node {

        final ArrayList<String> mKeys = new ArrayList<String>();
        final ArrayList<ValueNode> mValues = new ArrayList<ValueNode>();

        public void add(String key, ValueNode value) {
            mKeys.add(key);
            mValues.add(value);
        }

        public Map<String, ValueNode> getMap() {
            HashMap<String, ValueNode> retval = new HashMap<String, ValueNode>();
            for(int i=0;i<mKeys.size();i++) {
                retval.put(mKeys.get(i), mValues.get(i));
            }
            return retval;
        }
    }


}
