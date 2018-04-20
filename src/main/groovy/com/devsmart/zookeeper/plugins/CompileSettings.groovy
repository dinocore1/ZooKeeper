package com.devsmart.zookeeper.plugins

import com.devsmart.zookeeper.DefaultLibrary

class CompileSettings {

    final LinkedHashSet<String> flags = []
    final LinkedHashSet<File> includes = []
    final LinkedHashSet<DefaultLibrary> staticLinkedLibs = []
    final LinkedHashSet<DefaultLibrary> sharedLinkedLibs = []

}
