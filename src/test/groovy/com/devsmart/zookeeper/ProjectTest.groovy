package com.devsmart.zookeeper

import com.devsmart.zookeeper.api.FileCollection
import com.devsmart.zookeeper.api.FileTree
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

    @Test
    void testCreateFiles2() {

        File projectDir = folder.newFolder()

        File cool = new File(projectDir, 'src')
        cool = new File(cool, 'cool.txt')
        cool.parentFile.mkdirs()
        FileWriter writer = new FileWriter(cool)
        writer.append("this is the contents")
        writer.close()


        Project project = new Project(projectDir, null)

        FileCollection fc = project.files('src/pr_data.c', 'src/xrayvars.c', 'src/xrayglob.c', 'src/xrayfiles.c',
                'src/fluor_yield.c', 'src/coskron.c', 'src/crystal_diffraction.c',
                'src/scattering.c', 'src/fi.c', 'src/fii.c', 'src/splint.c')

        Set<File> files = fc.getFiles()
        assertFalse(files.isEmpty())

    }

    @Test
    void testCreateFileTree() {
        File projectDir = folder.newFolder()

        File cool = new File(projectDir, 'src')
        cool = new File(cool, 'cool.txt')
        cool.parentFile.mkdirs()
        FileWriter writer = new FileWriter(cool)
        writer.append("this is the contents")
        writer.close()


        Project project = new Project(projectDir, null)

        FileTree ft = project.fileTree(dir: 'src', include: '**/*.txt')
        File f1 = ft.getSingleFile()
        assertEquals(cool, f1)
    }
}
