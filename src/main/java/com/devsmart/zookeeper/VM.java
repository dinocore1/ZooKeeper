package com.devsmart.zookeeper;


import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VM {

    private static final Pattern VARABLE_REFERENCE = Pattern.compile("\\$\\{([a-zA-Z_]+)\\}");


    public static class Scope {

        Scope mParent;
        final TreeMap<String, String> mVarables = new TreeMap<String, String>();

        public String resolve(String varName) {
            String value = mVarables.get(varName);
            if(value != null) {
                return value;
            } else if(mParent != null) {
                return mParent.resolve(varName);
            } else {
                return "";
            }
        }

        public void setVar(String varName, String value) {
            mVarables.put(varName, value);
        }
    }

    private Scope mScope;

    public VM() {
        mScope = new Scope();
    }

    public Scope push() {
        Scope newScope = new Scope();
        newScope.mParent = mScope;
        mScope = newScope;
        return newScope;
    }

    public Scope pop() {
        Scope retval = mScope;
        if(mScope.mParent != null) {
            mScope = mScope.mParent;
        }

        return retval;
    }


    public String interpretString(String input) {
        StringBuffer sb = new StringBuffer();

        Matcher m = VARABLE_REFERENCE.matcher(input);
        while(m.find()) {
            final String var = m.group(1);
            String value = resolveVar(var);
            value = Matcher.quoteReplacement(value);
            m.appendReplacement(sb, value);
        }
        m.appendTail(sb);
        return sb.toString();

    }

    public String resolveVar(String varName) {
        return mScope.resolve(varName);
    }

    public void setVar(String varName, String value) {
        mScope.setVar(varName, value);
    }

    public void setVar(Map<String, String> varMap) {
        mScope.mVarables.putAll(varMap);
    }
}
