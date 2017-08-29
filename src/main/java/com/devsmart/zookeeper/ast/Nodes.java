package com.devsmart.zookeeper.ast;


import com.devsmart.zookeeper.*;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import org.antlr.v4.runtime.Token;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class Nodes {

    public static abstract class Node {

    }

    public static String escapeStringLiteral(String input) {
        input = input.substring(1, input.length()-1);
        input = input.replaceAll("\\\\\"", "\"");
        input = input.replace("\\\\", "\\");
        return input;
    }

    public static class FileNode extends Node {
        public final List<LibNode> libraries = new ArrayList<LibNode>();
    }

    public static class VersionNode extends Node {
        public final Version version = new Version();

    }

    public static class PlatformNode extends Node {
        public final ZooKeeperParser.PlatformContext mCtx;
        public Platform platform;
        public KeyValues keyValues;

        public PlatformNode(ZooKeeperParser.PlatformContext ctx) {
            mCtx = ctx;
        }
    }

    public static class LibNode extends Node {
        public final ZooKeeperParser.LibraryContext mCtx;
        public Library library;
        public SourceLocation src;
        public final List<Library> compileLibDependencies = new ArrayList<Library>();
        public final List<Library> testLibDependencies = new ArrayList<Library>();
        public KeyValues cmakeArgs;

        public LibNode(ZooKeeperParser.LibraryContext ctx) {
            mCtx = ctx;
        }
    }

    public static class KeyValues extends Node {
        public final ZooKeeperParser.KeyvaluesContext mCtx;
        public final List<KeyValue> keyValues = new ArrayList<KeyValue>();

        public KeyValues(ZooKeeperParser.KeyvaluesContext ctx) {
            mCtx = ctx;
        }

        public boolean hasKey(final String key) {
            return Iterables.tryFind(keyValues, new Predicate<KeyValue>() {
                @Override
                public boolean apply(KeyValue input) {
                    return input.getKey().equals(key);
                }
            }).isPresent();
        }

        public String getValue(final String key) {
            return Iterables.find(keyValues, new Predicate<KeyValue>() {
                @Override
                public boolean apply(KeyValue input) {
                    return input.getKey().equals(key);
                }
            }).getValue();
        }

        public Map<String, String> asMap() {
            TreeMap<String, String> retval = new TreeMap<String, String>();
            for(KeyValue entry : keyValues) {
                retval.put(entry.getKey(), entry.getValue());
            }
            return retval;
        }
    }

    public static class KeyValue extends Node {

        public final ZooKeeperParser.KeyvalueContext mCtx;

        public KeyValue(ZooKeeperParser.KeyvalueContext ctx) {
            mCtx = ctx;
        }

        public String getKey() {
            return mCtx.key.getText();
        }

        public Token getKeyToken() {
            return mCtx.key;
        }

        public String getValue() {
            return escapeStringLiteral(mCtx.value.getText());
        }

        public Token getValueToken() {
            return mCtx.value;
        }
    }
}
