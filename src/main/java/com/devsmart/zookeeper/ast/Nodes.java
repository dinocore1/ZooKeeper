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

    public static class VersionNode extends Node {
        public final Version version = new Version();

    }


}
