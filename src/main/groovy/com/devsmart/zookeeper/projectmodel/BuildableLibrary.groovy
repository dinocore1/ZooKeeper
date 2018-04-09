package com.devsmart.zookeeper.projectmodel

class BuildableLibrary extends BuildableModule implements Library {

    LinkType type
    Closure headers

    void type(String str) {
        this.type = LinkType.valueOf(str)
    }

    void exportHeaders(Closure cl) {

    }
}
