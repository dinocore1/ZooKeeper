package com.devsmart.zookeeper;


import com.devsmart.StringUtils;
import com.devsmart.zookeeper.tasks.ProcessBuildTask;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.collect.*;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.io.File;
import java.util.*;

public class CompilerConfig {

    private static final Function<String, String> TRIM = new Function<String, String>() {
        @Override
        public String apply(String input) {
            return input.trim();
        }
    };

    private static final Predicate<String> KEEP_NON_EMPTY = new Predicate<String>() {
        @Override
        public boolean apply(String input) {
            return !StringUtils.isEmptyString(input);
        }
    };

    private static final Joiner JOINER = Joiner.on(" ");


    public static final String INPUT = "input";
    public static final String OUTPUT = "output";
    public static final String WORKING_DIR = "workingDir";


    private final JsonObject mCfg;
    public final String name;


    public CompilerConfig(JsonObject cfg) {
        mCfg = cfg;

        name = cfg.get("name").getAsString();

    }

    public void configCompileTask(ProcessBuildTask task, ZooKeeper zooKeeper, ImmutableList<String> tags) {

        zooKeeper.mVM.push();
        try {

            String workingDir = overideOption(WORKING_DIR, mCfg, tags);
            workingDir = zooKeeper.mVM.interpretString(workingDir);
            zooKeeper.mVM.setVar(WORKING_DIR, workingDir);
            task.mExeDir = new File(workingDir);

            zooKeeper.mVM.setVar("flags", appendOption("compileFlags", mCfg, tags));

            String cmdLine = overideOption("compileCmd", mCfg, tags);

            cmdLine = zooKeeper.mVM.interpretString(cmdLine);

            setCmdline(task, cmdLine);


        } finally {
            zooKeeper.mVM.pop();
        }


    }

    public void configLinkTask(ProcessBuildTask task, ZooKeeper zooKeeper, ImmutableList<String> tags) {
        zooKeeper.mVM.push();
        try {

            String workingDir = overideOption(WORKING_DIR, mCfg, tags);
            workingDir = zooKeeper.mVM.interpretString(workingDir);
            zooKeeper.mVM.setVar(WORKING_DIR, workingDir);
            task.mExeDir = new File(workingDir);

            zooKeeper.mVM.setVar("flags", appendOption("linkFlags", mCfg, tags));

            String cmdLine = overideOption("linkCmd", mCfg, tags);

            cmdLine = zooKeeper.mVM.interpretString(cmdLine);

            setCmdline(task, cmdLine);


        } finally {
            zooKeeper.mVM.pop();
        }
    }

    private static String toString(Iterable<String> stringSet) {
        stringSet = Iterables.transform(stringSet, TRIM);
        stringSet = Iterables.filter(stringSet, KEEP_NON_EMPTY);
        return JOINER.join(stringSet);
    }

    private static String overideOption(String name, JsonObject cfg, ImmutableList<String> tags) {
        JsonElement element;
        String retval = null;

        if((element = cfg.get(name)) != null) {
            if(element.isJsonPrimitive()) {
                retval = element.getAsString();
            }
        }

        for(String context : tags) {
            if((element = cfg.get(context)) != null && element.isJsonObject()) {
                String deeperValue = overideOption(name, element.getAsJsonObject(), tags);
                if(deeperValue != null) {
                    retval = deeperValue;
                }
            }
        }

        return retval;
    }

    private static String appendOption(String name, JsonObject cfg, ImmutableList<String> tags) {
        LinkedHashSet<String> retval = new LinkedHashSet<>();
        appendOption(name, cfg, tags, retval);
        return toString(retval);
    }

    private static void appendOption(String name, JsonObject cfg, ImmutableList<String> tags, LinkedHashSet<String> retval) {
        JsonElement element;

        if((element = cfg.get(name)) != null) {
            if(element.isJsonPrimitive()) {
                retval.add(element.getAsString());
            } else if(element.isJsonArray()) {
                JsonArray array = element.getAsJsonArray();
                for(int i=0;i<array.size();i++) {
                    retval.add(array.get(i).getAsString());
                }
            }
        }

        for(String context : tags) {
            if((element = cfg.get(context)) != null && element.isJsonObject()) {
                appendOption(name, element.getAsJsonObject(), tags, retval);
            }
        }
    }

    private void setCmdline(ProcessBuildTask task, String cmdLine) {
        Collection<String> collection = Collections2.transform(Arrays.asList(cmdLine.split(" ")), new Function<String, String>() {
            @Override
            public String apply(String input) {
                return input.trim();
            }
        });

        collection = Collections2.filter(collection, new Predicate<String>() {
            @Override
            public boolean apply(String input) {
                return !StringUtils.isEmptyString(input);
            }
        });

        task.commandLine.addAll(collection);


    }

    private void pre(List<String> list, JsonElement element) {
        if(element != null) {
            if (element.isJsonArray()) {
                JsonArray array = element.getAsJsonArray();
                for (int i = 0; i < array.size(); i++) {
                    list.add(0, array.get(i).getAsString());
                }
            } else if (element.isJsonPrimitive()) {
                list.addAll(0, Arrays.asList(element.getAsJsonPrimitive().getAsString().split(" ")));
            }
        }
    }

    private String pre(String string, JsonElement element) {
        if(element != null) {
            if (element.isJsonArray()) {
                JsonArray array = element.getAsJsonArray();
                for (int i = 0; i < array.size(); i++) {
                    string = array.get(i).getAsString().trim() + " " + string;
                }
            } else if (element.isJsonPrimitive()) {
                string = element.getAsString().trim() + " " + string;
            }
        }
        return string.trim();
    }
}
