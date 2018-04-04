package com.devsmart.zookeeper

import com.devsmart.zookeeper.api.FileCollection

class ApplyTemplate {


    FileCollection input
    FileCollection output
    List<String> flags = []
    List<File> includes = []

    void flags(String... f) {
        flags.addAll(f)
    }

    void includes(FileCollection fc) {
        includes.addAll(fc.files)
    }

    static Closure prefix(String prefix, Iterable list) {
        return {
            list.collect({
                "${prefix}$it"
            })
        }
    }

}
