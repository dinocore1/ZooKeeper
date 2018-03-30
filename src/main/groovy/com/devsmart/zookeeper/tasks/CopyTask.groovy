package com.devsmart.zookeeper.tasks

import com.devsmart.zookeeper.api.FileCollection

class CopyTask implements BuildTask {

    FileCollection from
    File to


    def from(String... paths) {

    }

    def to(String path) {

    }


    @Override
    boolean run() {




        return false
    }
}
