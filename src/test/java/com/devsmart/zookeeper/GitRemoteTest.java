package com.devsmart.zookeeper;


import com.devsmart.IOUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.RemoteAddCommand;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.transport.URIish;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

public class GitRemoteTest {


    @Test
    public void testGitRemote() throws Exception {

        File localPath = new File("gittest.git");
        IOUtils.deleteTree(localPath);
        localPath.mkdirs();

        Git git = Git.init()
                .setGitDir(localPath)
                .setBare(true)
                .call();


        RemoteAddCommand remoteAdd = git.remoteAdd();
        remoteAdd.setName("origin");
        remoteAdd.setUri(new URIish("git@github.com:dinocore1/Staple.git"));
        remoteAdd.call();

        git.fetch()
                .setRemote("origin")
                //.setRefSpecs(new RefSpec("2a29f0c6e9ceea50750966abd4eff6c478bed73a"))
                .call();




        Collection<Ref> result = Git.lsRemoteRepository()
                .setRemote("git@github.com:dinocore1/Staple.git")
                .call();

        for (Ref ref : result) {
            System.out.println("Ref: " + ref);
        }


    }
}
