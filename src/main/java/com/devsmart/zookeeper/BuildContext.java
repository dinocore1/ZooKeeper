package com.devsmart.zookeeper;


import com.google.common.hash.HashCode;
import com.google.common.util.concurrent.Atomics;

import java.io.File;
import java.util.concurrent.atomic.AtomicReference;

public class BuildContext {

    public final Library library;
    public final Platform platform;

    public final AtomicReference<HashCode> buildHash = Atomics.newReference();
    public AtomicReference<File> sourceDir = Atomics.newReference();
    public AtomicReference<File> buildDir = Atomics.newReference();
    public AtomicReference<File> installDir = Atomics.newReference();

    public BuildContext(Library library, Platform platform) {
        this.library = library;
        this.platform = platform;
    }

    public LibraryPlatformKey getPlatformKey() {
        return new LibraryPlatformKey(library, platform);
    }
}
