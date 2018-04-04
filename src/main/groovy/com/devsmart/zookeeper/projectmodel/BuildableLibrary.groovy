package com.devsmart.zookeeper.projectmodel

class BuildableLibrary extends BuildableModule implements Library {

    LinkType type

    void type(String str) {
        this.type = LinkType.valueOf(str)
    }
}
