package com.devsmart.zookeeper;


import com.google.common.io.BaseEncoding;
import org.slf4j.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Random;

public class Utils {

    public static String captialFirstLetter(String str) {
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    public static String createActionName(String verb, String noun) {
        return verb + captialFirstLetter(noun);
    }

    public static Runnable createInputStreamLogAppender(final InputStream in, final Logger logger) {
        return new Runnable() {
            @Override
            public void run() {
                try {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));

                    String line;
                    while((line = reader.readLine()) != null) {
                        logger.info(">> " + line);
                    }

                } catch (Exception e) {
                    logger.error("", e);
                }
            }
        };
    }

    public static File createTempDir(File root) {
        Random random = new Random();
        byte[] data = new byte[3];
        random.nextBytes(data);
        return new File(root, BaseEncoding.base16().encode(data));
    }
}
