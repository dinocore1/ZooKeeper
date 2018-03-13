package com.devsmart.zookeeper;


import com.devsmart.StringUtils;
import com.devsmart.zookeeper.tasks.ProcessBuildTask;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class CompilerConfig {


    public static final String INPUT = "input";
    public static final String OUTPUT = "output";


    private final JsonObject mCfg;
    public final String name;


    public CompilerConfig(JsonObject cfg) {
        mCfg = cfg;

        name = cfg.get("name").getAsString();



    }

    public void createDebugCompileTask(ProcessBuildTask compileTask, ZooKeeper zooKeeper) {
        JsonElement element;

        String compileflags = pre("", mCfg.get("compileFlags"));


        if((element = mCfg.get("debug")) != null && element.isJsonObject()){
            compileflags = pre(compileflags, element.getAsJsonObject().get("compileFlags"));
        }

        zooKeeper.mVM.push();
        try {

            zooKeeper.mVM.setVar("flags", compileflags);

            String cmdline = mCfg.get("compileCmd").getAsString();

            cmdline = zooKeeper.mVM.interpretString(cmdline);

            setCmdline(compileTask, cmdline);

        } finally {
            zooKeeper.mVM.pop();
        }


    }

    public void createLinkerTask(ProcessBuildTask linkerTask, ZooKeeper zooKeeper) {
        JsonElement element;

        String exeLinkerFlags = pre("", mCfg.get("exeLinkerFlags"));


        zooKeeper.mVM.push();
        try {

            zooKeeper.mVM.setVar("flags", exeLinkerFlags);

            String cmdline = mCfg.get("exeLinkerCmd").getAsString();

            cmdline = zooKeeper.mVM.interpretString(cmdline);

            setCmdline(linkerTask, cmdline);

        } finally {
            zooKeeper.mVM.pop();
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
