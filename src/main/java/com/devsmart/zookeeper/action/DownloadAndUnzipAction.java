package com.devsmart.zookeeper.action;

import com.devsmart.IOUtils;
import com.devsmart.zookeeper.Action;
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

        } catch (IOException e) {
            LOGGER.error("", e);
        }

    }
}
