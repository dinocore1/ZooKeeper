package com.devsmart.zookeeper.ast;


import com.devsmart.zookeeper.Library;
import com.devsmart.zookeeper.Version;
import com.devsmart.zookeeper.ZooKeeperParser;
import com.sun.xml.internal.messaging.saaj.packaging.mime.internet.HeaderTokenizer;
import org.antlr.v4.runtime.Token;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public static class LibNode extends Node {
        public final ZooKeeperParser.LibraryContext mCtx;
        public Library library;
        public String src;
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
