package com.devsmart.zookeeper;

import com.devsmart.zookeeper.projectmodel.Library;
import com.devsmart.zookeeper.projectmodel.LinkType;

public class LinkableLibrary extends DefaultLibrary {

    public final LinkType linkType;

    public LinkableLibrary(String name, Version version, LinkType type) {
        super(name, version);
        this.linkType = type;
    }

    public LinkableLibrary(Library lib, LinkType type) {
        super(lib.getName(), lib.getVersion());
        this.linkType = type;
    }
}
