package com.devsmart.zookeeper.projectmodel;

import com.devsmart.zookeeper.Version;

import java.util.Set;

public interface Module {

    String getName();
    Version getVersion();
    Set<Library> getDependencies();

}
