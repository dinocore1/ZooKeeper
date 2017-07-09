package com.devsmart.zookeeper.action;

import com.devsmart.IOUtils;
import com.devsmart.zookeeper.*;
import com.google.common.hash.HashCode;
import com.google.common.io.BaseEncoding;
import com.google.common.io.Files;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Date;
import java.util.concurrent.atomic.AtomicReference;
import java.util.zip.ZipInputStream;


public class DownloadAndUnzipAction implements Action {

    private static final Logger LOGGER = LoggerFactory.getLogger(DownloadAndUnzipAction.class);

    public final String urlStr;
    private final ZooKeeper mZooKeeper;
    private final AtomicReference<File> mSourceRoot;

    public DownloadAndUnzipAction(String httpUrl, AtomicReference<File> sourceRoot, ZooKeeper zooKeeper) {
        urlStr = httpUrl;
        mSourceRoot = sourceRoot;
        mZooKeeper = zooKeeper;
    }

    private HashCode downloadAndUnzip() throws IOException {
        LOGGER.info("Downloading: {}", urlStr);

        File downloadTempDir = Utils.createTempDir(mZooKeeper.mZooKeeperRoot);

        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpGet request = new HttpGet(urlStr);
        CloseableHttpResponse response = httpclient.execute(request);
        try {
            IOUtils.unzipFile(response.getEntity().getContent(), downloadTempDir, true);

        } finally {
            response.close();
        }

        //check if this zip file contained only 1 dir with all the code inside it.
        //If so, move everything into the dest dir.
        File[] unzipFileList = downloadTempDir.listFiles();
        if(unzipFileList.length == 1 && unzipFileList[0].isDirectory()) {
            for(File f : unzipFileList[0].listFiles()) {
                Files.move(f, new File(downloadTempDir, f.getName()));
            }
        }

        if(downloadTempDir.listFiles().length == 0) {
            throw new IOException("download contains no files: " + urlStr);
        }

        FSChecksum checksum = new FSChecksum(downloadTempDir);

        ZooKeeper.DownloadCache downloadCacheEntry = new ZooKeeper.DownloadCache();
        downloadCacheEntry.sourceHash = checksum.computeHash();
        downloadCacheEntry.downloadTime = new Date();

        final File sourceDir = getSourceDir(downloadCacheEntry.sourceHash);
        sourceDir.getParentFile().mkdirs();
        if(!sourceDir.exists()) {
            downloadTempDir.renameTo(sourceDir);
        } else {
            LOGGER.info("download source already exits");
            IOUtils.deleteTree(downloadTempDir);
        }

        mZooKeeper.mDownloadCache.put(urlStr, downloadCacheEntry);
        return downloadCacheEntry.sourceHash;
    }

    public File getSourceDir(HashCode sourceHash) {
        String sourceHashStr = BaseEncoding.base16().encode(sourceHash.asBytes());

        File sourceDir = new File(mZooKeeper.mZooKeeperRoot, "download");
        sourceDir = new File(sourceDir, sourceHashStr);
        return sourceDir;
    }

    @Override
    public void doIt() throws Exception {
        File sourceFile;
        HashCode sourceHash;
        ZooKeeper.DownloadCache cacheResult = mZooKeeper.mDownloadCache.get(urlStr);
        if(cacheResult == null || !(sourceFile = getSourceDir(cacheResult.sourceHash)).exists()) {
            sourceHash = downloadAndUnzip();
            sourceFile = getSourceDir(sourceHash);
        } else {
            LOGGER.info("{} from cache: {}", urlStr, sourceFile.getAbsolutePath());
        }

        mSourceRoot.set(sourceFile);

    }

    public static String createActionName(Library library) {
        return "download" + Utils.captialFirstLetter(library.name) + library.version.toString();
    }
}
