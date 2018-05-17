package com.devsmart.zookeeper.tasks

import com.devsmart.zookeeper.file.DefaultBaseDirFileResolver
import com.devsmart.zookeeper.file.DefaultFileTree
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import static org.junit.Assert.*
import org.junit.rules.TemporaryFolder

class CopyTaskTest {


    @Rule
    public TemporaryFolder folder = new TemporaryFolder()

    private DefaultBaseDirFileResolver fileResolver

    @Before
    void setup() {
        fileResolver = new DefaultBaseDirFileResolver(folder.getRoot())
    }

    @Test
    void testCopy1() {

        File root = folder.getRoot()

        File f1 = new File(root, "dir1")
        f1.mkdir()
        f1 = new File(f1, "f1.txt")
        FileWriter writer = new FileWriter(f1)
        writer.append("hello world")
        writer.close()

        String newDir = "${folder.root}" + File.separator + "newdir"

        CopyTask task = new CopyTask()
        task.from(new DefaultFileTree([dir: 'dir1', include: '*.txt'], fileResolver))
        task.into(newDir)

        assertTrue(task.run())

        File destFile = new File(root, 'newdir' + File.separator + "f1.txt")
        assertTrue(destFile.exists())


    }

}
