package com.devsmart.zookeeper.sourcelocation;

import com.devsmart.zookeeper.SourceLocation;


public class DownloadSourceLocation implements SourceLocation {

    public final String mUrl;

    public DownloadSourceLocation(String url) {
        mUrl = url;
    }
}
