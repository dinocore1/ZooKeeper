package com.devsmart.zookeeper.tasks

import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class CopyTaskTest {


    @Rule
    public TemporaryFolder folder = new TemporaryFolder()

    @Test
    void testCopy1() {

        File f1 = folder.newFile("f1.txt")

        String newDir = "${folder.root}" + File.separator + "newdir"

        CopyTask task = new CopyTask()
        task.to(newDir)


    }

}
