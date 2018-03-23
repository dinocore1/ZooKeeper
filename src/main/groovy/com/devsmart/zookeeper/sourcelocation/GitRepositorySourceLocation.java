package com.devsmart.zookeeper.sourcelocation;

import com.devsmart.zookeeper.Action;
import com.devsmart.zookeeper.SourceLocation;

import java.util.Map;


public class GitRepositorySourceLocation implements SourceLocation, Action {

    public final String gitURL;
    public final String gitRev;

    public GitRepositorySourceLocation(Map<String, String> args) {
        gitURL = args.get("url");
        gitRev = args.get("rev");
    }

    @Override
    public void doIt() throws Exception {

    }
}
