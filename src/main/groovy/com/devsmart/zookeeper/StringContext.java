package com.devsmart.zookeeper;

import com.google.common.base.Preconditions;
import org.codehaus.groovy.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringContext {


    private static final Logger LOGGER = LoggerFactory.getLogger(StringContext.class);

    private HashMap<CharSequence, Pattern> mPatterns = new HashMap<>();
    private HashMap<CharSequence, CharSequence> mVars = new HashMap<>();

    public void setVar(CharSequence key, CharSequence value) {
        mPatterns.put(key, Pattern.compile("\\$\\(" + key + "\\)"));
        mVars.put(key, value);
    }

    public CharSequence resolve(CharSequence input) {

        for(CharSequence key : mVars.keySet()) {
            input = replace(input, key);
        }

        return input;
    }

    private String replace(CharSequence input, CharSequence var) {
        Pattern pattern = mPatterns.get(var);
        final CharSequence value = mVars.get(var);
        Matcher matcher = pattern.matcher(input);

        StringBuffer builder = new StringBuffer();
        while (matcher.find()) {
            String v = value.toString();
            if(v.endsWith("\\")) {
                v = v + "\\";
            }
            matcher.appendReplacement(builder, v);
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
