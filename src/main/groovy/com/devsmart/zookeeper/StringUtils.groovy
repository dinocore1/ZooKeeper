package com.devsmart.zookeeper

class StringUtils {


    static String toCamelCase(String... strs) {
        StringBuilder builder = new StringBuilder()
        builder.append(strs[0].toLowerCase())

        for(int i=1;i<strs.length;i++) {
            String name = strs[i]
            builder.append(name.charAt(0).toUpperCase())
            if(name.length() > 1) {
                builder.append(name.substring(1, strs[i].length()))
            }
        }
        return builder.toString()
    }


}
