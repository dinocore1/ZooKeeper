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

    static String[] flatten(Object obj) {
        if(obj instanceof Collection) {
            return ((Collection) obj).collect({ it ->
                return it.toString()
            }) as String[]
        } else {
            return obj.toString()
        }
    }

    static void mergeStringMaps(Map<String, String> src, Map<String, String> dest) {
        StringContext strEnv = new StringContext();
        strEnv.putAll(dest);

        for(Map.Entry<String, String> entry : src) {
            final String key = entry.getKey();
            CharSequence value = entry.getValue();

            value = strEnv.resolve(value);
            dest.put(key, value.toString());
            strEnv.setVar(key, value);
        }
    }


}
