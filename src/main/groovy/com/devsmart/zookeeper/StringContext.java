package com.devsmart.zookeeper;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringContext {

    private HashMap<String, Pattern> mPatterns = new HashMap<>();
    private HashMap<String, String> mVars = new HashMap<>();

    public void setVar(String key, String value) {
        mPatterns.put(key, Pattern.compile("\\$\\(" + key + "\\)"));
        mVars.put(key, value);
    }

    public String resolve(String input) {

        for(String key : mVars.keySet()) {
            input = replace(input, key);
        }

        return input;
    }

    private String replace(String input, String var) {
        Pattern pattern = mPatterns.get(var);
        final String value = mVars.get(var);
        Matcher matcher = pattern.matcher(input);

        StringBuffer builder = new StringBuffer();
        while(matcher.find()) {
            matcher.appendReplacement(builder, value);
        }
        matcher.appendTail(builder);

        return builder.toString();
    }

    public void putAll(Map<String, String> values) {
        for(Map.Entry<String, String> entry : values.entrySet()) {
            setVar(entry.getKey(), entry.getValue());
        }
    }
}
