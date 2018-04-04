package com.devsmart.zookeeper.projectmodel

import com.devsmart.zookeeper.api.FileCollection

class BuildableLibrary extends BuildableModule implements Library {

    LinkType type
    Closure headers

    void type(String str) {
        this.type = LinkType.valueOf(str)
    }

    void exportHeaders(Closure cl) {

    }
}
