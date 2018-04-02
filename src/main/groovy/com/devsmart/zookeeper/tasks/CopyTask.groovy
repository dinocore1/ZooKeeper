package com.devsmart.zookeeper.tasks

import com.devsmart.zookeeper.api.FileCollection

class CopyTask implements BuildTask {

    FileCollection from
    File into


    def from(String... paths) {

    }

    def into(String path) {

    }


    @Override
    boolean run() {




        return false
    }
}
