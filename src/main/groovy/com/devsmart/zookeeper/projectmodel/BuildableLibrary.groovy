package com.devsmart.zookeeper.projectmodel

import com.devsmart.zookeeper.AbstractLibrary

class BuildableLibrary extends BuildableModule implements Library {

    LinkType type
    Closure exportHeaderCl

    void type(String str) {
        this.type = LinkType.valueOf(str)
    }

    void exportHeaders(Closure cl) {
        exportHeaderCl = cl
    }

    @Override
    int hashCode() {
        return AbstractLibrary.libraryHashCode(this)
    }

    @Override
    boolean equals(Object o) {
        if(o == null) {
            return false
        }
        if(o instanceof Library) {
            return AbstractLibrary.libraryEquals(this, (Library)o)
        } else {
            return false
        }
    }

    @Override
    String toString() {
        return String.format("%s:%s", name, version)
    }
}
