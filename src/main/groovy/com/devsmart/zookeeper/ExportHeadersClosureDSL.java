package com.devsmart.zookeeper;

public class ExportHeadersClosureDSL {

    private String mFromPath;
    private String mIncludeSpec = "**/*.h";

    void from(String path) {
        mFromPath = path;
    }

    void include(String includeSpec) {
        mIncludeSpec = includeSpec;
    }


}
