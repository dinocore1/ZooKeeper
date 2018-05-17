package com.devsmart.zookeeper.tasks

import com.devsmart.zookeeper.api.FileDetails
import com.devsmart.zookeeper.api.FileTree
import com.devsmart.zookeeper.api.FileVisitor
import org.apache.commons.io.IOUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class CopyTask implements BuildTask {

    private Logger LOGGER = LoggerFactory.getLogger(CopyTask.class)

    FileTree from
    File into


    void from(FileTree tree) {
        this.from = tree

    }

    void into(String path) {
        this.into = new File(path)
    }

    void into(File f) {
        this.into = f
    }


    @Override
    boolean run() {

        boolean success

        success = into.mkdirs()

        from.visit(new FileVisitor() {
            @Override
            void visit(FileDetails fileDetails) {

                if(fileDetails.isDirectory()) {
                    File dest = new File(into, fileDetails.getRelativePath().getPathString())
                    success &= dest.mkdirs()

                } else {

                    File src = fileDetails.file
                    File dest = new File(into, fileDetails.getRelativePath().getPathString())

                    LOGGER.info("copying {} to {}", src, dest)

                    FileInputStream input = new FileInputStream(src)
                    FileOutputStream output = new FileOutputStream(dest)

                    try {
                        IOUtils.copyLarge(input, output)
                    } catch (IOException e) {
                        LOGGER.error("", e)
                        success = false
                    }

                    output.close()
                    input.close()
                }


            }
        })



        return success
    }
}
