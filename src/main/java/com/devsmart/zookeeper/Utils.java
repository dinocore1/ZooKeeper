package com.devsmart.zookeeper;


public class Utils {

    public static String captialFirstLetter(String str) {
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    public static String createActionName(String verb, String noun) {
        return verb + captialFirstLetter(noun);
    }
}
