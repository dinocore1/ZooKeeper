package com.devsmart.zookeeper.action;

import com.devsmart.zookeeper.Library;
import com.devsmart.zookeeper.Platform;
import com.devsmart.zookeeper.Utils;

public class SecureLibraryAction extends PhonyAction {

    public static String createActionName(Library library, Platform platform) {
        return "secure" + Utils.captialFirstLetter(library.toString()) + Utils.captialFirstLetter(platform.toString());
    }

}
