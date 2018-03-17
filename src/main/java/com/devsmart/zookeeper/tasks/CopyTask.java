package com.devsmart.zookeeper.tasks;

import com.devsmart.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

public class CopyTask implements BuildTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(CopyTask.class);

    private final File src;
    private final File dest;

    public CopyTask(File src, File dest) {
        this.src = src;
        this.dest = dest;
    }

    @Override
    public boolean run() {
        return copy(src, dest);
    }

    public boolean copy(File src, File dest) {
        if(!src.exists()) {
            return false;
        }

        if(src.isDirectory()) {
            if(!dest.exists()) {
                if(!dest.mkdirs()){
                    return false;
                }
            }

            for(File f : src.listFiles()) {
                if(!copy(new File(src, f.getName()), new File(dest, f.getName()))){
                    return false;
                }
            }

        } else {
            try {
                InputStream in = new FileInputStream(src);
                OutputStream out = new FileOutputStream(dest);
                IOUtils.pump(in, out);
            } catch (IOException e) {
                LOGGER.error("", e);
                return false;
            }

        }

        return true;
    }
}
