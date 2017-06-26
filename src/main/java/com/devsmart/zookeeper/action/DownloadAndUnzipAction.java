package com.devsmart.zookeeper.action;

import com.devsmart.IOUtils;
import com.devsmart.zookeeper.Action;
import com.google.common.io.Files;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;


public class DownloadAndUnzipAction implements Action {

    private static final Logger LOGGER = LoggerFactory.getLogger(DownloadAndUnzipAction.class);

    private final String mUrlStr;
    private final File mDestDir;

    public DownloadAndUnzipAction(String httpUrl, File destDir) {
        mUrlStr = httpUrl;
        mDestDir = destDir;
    }

    @Override
    public void doIt() {

        try {
            mDestDir.mkdirs();

            LOGGER.info("Downloading: " + mUrlStr);
            InputStream zipInputStream = new URL(mUrlStr).openStream();
            IOUtils.unzipFile(zipInputStream, mDestDir, true);


            //check if this zip file contained only 1 dir with all the code inside it.
            //If so, move everything into the dest dir.
            File[] unzipFileList = mDestDir.listFiles();
            if(unzipFileList.length == 1 && unzipFileList[0].isDirectory()) {
                for(File f : unzipFileList[0].listFiles()) {
                    Files.move(f, new File(mDestDir, f.getName()));
                }
            }

        } catch (IOException e) {
            LOGGER.error("", e);
        }

    }
}
