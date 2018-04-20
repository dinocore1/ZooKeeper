package com.devsmart.zookeeper.plugins

import com.devsmart.zookeeper.projectmodel.AbstractLibrary

class CompileSettings {

    final LinkedHashSet<String> flags = []
    final LinkedHashSet<File> includes = []
    final LinkedHashSet<AbstractLibrary> staticLinkedLibs = []
    final LinkedHashSet<AbstractLibrary> sharedLinkedLibs = []

}
