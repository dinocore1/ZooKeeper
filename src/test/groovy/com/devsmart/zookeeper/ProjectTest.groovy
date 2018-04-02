package com.devsmart.zookeeper

import com.devsmart.zookeeper.api.FileCollection
import org.junit.Rule
import org.junit.Test
import static org.junit.Assert.*
import org.junit.rules.TemporaryFolder

class ProjectTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder()

    @Test
    void testCreateFiles() {
        File projectDir = folder.newFolder()

        File cool = new File(projectDir, 'src')
        cool = new File(cool, 'cool.txt')
        cool.parentFile.mkdirs()
        FileWriter writer = new FileWriter(cool)
        writer.append("this is the contents")
        writer.close()


        Project project = new Project(projectDir, null)

        FileCollection fc = project.files('src/cool.txt')
        File f1 = fc.getSingleFile()
        assertEquals(cool, f1)

    }
}
