package com.devsmart.zookeeper;


import com.google.common.io.Resources;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.*;

public class ParserTest {


    @Test
    public void testParseZooFile() throws Exception {
        ZooKeeper zooKeeper = new ZooKeeper();
        assertTrue(zooKeeper.compile(Resources.getResource("library.zoo").openStream()));

        Action checkAction = zooKeeper.mDependencyGraph.getAction("all");
        assertNotNull(checkAction);
        zooKeeper.mDependencyGraph.runAction(checkAction);
    }
}
