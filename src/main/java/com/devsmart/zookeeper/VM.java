package com.devsmart.zookeeper;


import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VM {

    private static final Pattern VARABLE_REFERENCE = Pattern.compile("\\$\\{([a-zA-Z_]+)\\}");

    private final TreeMap<String, String> mVarables = new TreeMap<String, String>();


    public String interpretString(String input) {
        StringBuffer sb = new StringBuffer();

        Matcher m = VARABLE_REFERENCE.matcher(input);
        while(m.find()) {
            final String var = m.group(1);
            String value = resolveVar(var);
            m.appendReplacement(sb, value);
        }
        m.appendTail(sb);
        return sb.toString();

    }

    public String resolveVar(String varName) {
        String value = mVarables.get(varName);
        if(value == null) {
            value = "";
        }
        return value;
    }

    public void setVar(String varName, String value) {
        mVarables.put(varName, value);
    }

    public void setVar(Map<String, String> varMap) {
        mVarables.putAll(varMap);
    }
}
