package com.devsmart.zookeeper.ast;


import com.devsmart.ArrayTable;
import com.devsmart.zookeeper.*;
import com.google.common.base.Joiner;
import com.google.common.collect.ComparisonChain;
import org.jetbrains.annotations.NotNull;

import java.io.File;
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

        @Override
        public String toString() {
            return version.toString();
        }
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
        public boolean isString() {
            return false;
        }
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
        public boolean isString() {
            return true;
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
            builder.append('[');
            joiner.appendTo(builder, array);
            builder.append(']');
            return builder.toString();
        }
    }

    public static class ObjectNode extends ValueNode {
        public final KeyValueEntriesNode entries;

        public ObjectNode(KeyValueEntriesNode entries) {
            this.entries = entries;

        }

        public Iterable<ValueNode> get(String key) {
            return entries.get(key);
        }

        @Override
        public boolean isObject() {
            return true;
        }


    }

    private static final ArrayTable.MultikeyBinarySearch BINARY_SEARCH = new ArrayTable.MultikeyBinarySearch.Builder()
            .addObj(0, new ArrayTable.StringRowComparator(0).getComparator())
            .build();

    public static class KeyValueEntriesNode extends Node {

        final ArrayTable mValues = ArrayTable.createWithColumnTypes(String.class, ValueNode.class);

        public void add(String key, ValueNode value) {

            int i;
            synchronized (BINARY_SEARCH) {
                BINARY_SEARCH.setKey(0, key);
                i = BINARY_SEARCH.search(mValues);
            }

            if(i < 0) {
                i = -i - 1;
            }
            mValues.insertAt(i, key, value);
        }

        public Iterable<ValueNode> get(String key) {
            int i;
            synchronized (BINARY_SEARCH) {
                BINARY_SEARCH.setKey(0, key);
                i = BINARY_SEARCH.search(mValues);
            }

            if(i < 0) {
                return Collections.emptyList();
            } else {
                ArrayList<ValueNode> retval = new ArrayList<ValueNode>(3);

                while(mValues.getObject(i, 0).equals(key)) {
                    ValueNode value = mValues.getObject(i, 1);
                    retval.add(value);
                    i++;
                }
                return retval;
            }
        }
    }


}
