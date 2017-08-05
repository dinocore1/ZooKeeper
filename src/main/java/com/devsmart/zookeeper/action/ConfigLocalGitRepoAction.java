package com.devsmart.zookeeper.action;

import com.devsmart.zookeeper.Action;
import com.devsmart.zookeeper.Library;
import com.devsmart.zookeeper.ZooKeeper;
import com.devsmart.zookeeper.sourcelocation.GitRepositorySourceLocation;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.RemoteAddCommand;
import org.eclipse.jgit.api.RemoteRemoveCommand;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.transport.RemoteConfig;
import org.eclipse.jgit.transport.URIish;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;


public class ConfigLocalGitRepoAction implements Action {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigLocalGitRepoAction.class);


    private final GitRepositorySourceLocation mConfig;
    private final AtomicReference<File> mSourceRoot;
    private final ZooKeeper mZooKeeper;
    private final Library mLibrary;

    public ConfigLocalGitRepoAction(Library library, GitRepositorySourceLocation config, AtomicReference<File> sourceDir, ZooKeeper zooKeeper) {
        mLibrary = library;
        mConfig = config;
        mSourceRoot = sourceDir;
        mZooKeeper = zooKeeper;
    }

    private static boolean originExists(List<RemoteConfig> configs) {
        return Iterables.tryFind(configs, new Predicate<RemoteConfig>() {
            @Override
            public boolean apply(RemoteConfig input) {
                return input.getName().equals("origin");
            }
        }).isPresent();
    }

    @Override
    public void doIt() throws Exception {

        File localCloneDir = new File(mZooKeeper.mZooKeeperRoot, "git");
        localCloneDir = new File(localCloneDir, mLibrary.name);

        Git git;
        if(!localCloneDir.exists()) {
            localCloneDir.mkdirs();
            git = Git.init()
                    .setDirectory(localCloneDir)
                    .call();
        } else {
            git = Git.open(localCloneDir);
        }

        if(originExists(git.remoteList().call())) {
            RemoteRemoveCommand removeCommand = git.remoteRemove();
            removeCommand.setName("origin");
            removeCommand.call();
        }

        RemoteAddCommand addCommand = git.remoteAdd();
        addCommand.setName("origin");
        addCommand.setUri(new URIish(mConfig.gitURL));
        addCommand.call();

        LOGGER.info("fetching from: {}", mConfig.gitURL);
        git.fetch().call();

        LOGGER.info("checking out: {}", mConfig.gitRev);
        git.checkout()
                .setName(mConfig.gitRev)
                .call();

        mSourceRoot.set(localCloneDir);
    }
}
