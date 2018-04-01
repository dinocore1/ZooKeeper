package com.devsmart.zookeeper.file;

import java.io.File;

public interface PathToFileResolver {

    File resolve(Object path);
}
