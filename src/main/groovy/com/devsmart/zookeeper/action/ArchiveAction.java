package com.devsmart.zookeeper.action;

import com.devsmart.IOUtils;
import com.devsmart.zookeeper.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;


public class ArchiveAction implements Action {

    private final CMakeBuildContext mContext;

    public static String createActionName(LibraryPlatformKey libraryPlatformKey) {
        return Utils.createActionName("archive", libraryPlatformKey.toString());
    }

    public static String createArchiveFile(BuildContext buildContext) {
        String filename = String.format("%s-%s-%s.zip",
                buildContext.getPlatformKey().toString(),
                buildContext.library.version,
                buildContext.buildHash.get().toString().substring(0, 6)
                );
        return filename;
    }

    public ArchiveAction(CMakeBuildContext buildContext) {
        mContext = buildContext;

    }

    @Override
    public void doIt() throws Exception {
        File archiveFile = new File(mContext.zookeeper.mZooKeeperRoot, "archives");
        archiveFile.mkdirs();
        archiveFile = new File(archiveFile, createArchiveFile(mContext));
        File installDir = mContext.installDir.get();


        ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(archiveFile));
        archiveDir(installDir, "", zipOut);
        zipOut.finish();
        zipOut.close();

    }

    private void archiveDir(File dir, String root, ZipOutputStream zipOut) throws IOException {
        for(File f : dir.listFiles()) {
            if(f.isFile()) {
                ZipEntry entry = new ZipEntry(root + f.getName());
                zipOut.putNextEntry(entry);
                FileInputStream fin = new FileInputStream(f);
                IOUtils.pump(fin, zipOut, true, false);
                zipOut.closeEntry();
            } else if(f.isDirectory()) {
                String newroot = root + f.getName() + "/";
                archiveDir(f, newroot, zipOut);
            }
        }
    }


}
