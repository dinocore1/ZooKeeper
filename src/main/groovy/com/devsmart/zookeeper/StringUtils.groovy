package com.devsmart.zookeeper

class StringUtils {


    static String toCamelCase(String... strs) {
        StringBuilder builder = new StringBuilder()
        builder.append(strs[0].toLowerCase())

        for(int i=1;i<strs.length;i++) {
            builder.append(strs[0].charAt(0).toUpperCase())
            if(strs[0].length() > 1) {
                builder.append(strs[0].substring(1, strs[0].length()))
            }
        }
        return builder.toString()
    }

}
