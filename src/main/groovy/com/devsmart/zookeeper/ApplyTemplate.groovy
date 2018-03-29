package com.devsmart.zookeeper

class ApplyTemplate {


    FileCollection input
    FileCollection output
    List<String> flags = []
    List<File> includes = []

    void flags(String... f) {
        flags.addAll(f)
    }

    static Closure prefix(String prefix, Iterable list) {
        return {
            list.collect({
                "${prefix}$it"
            })
        }
    }

}
