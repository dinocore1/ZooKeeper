package com.devsmart.zookeeper

import org.junit.Test

class TestDSL {

    @Test
    void testDSLTask() {

        ZooKeeper_DSL dsl = new ZooKeeper_DSL() {
            @Override
            Object run() {
                return null
            }
        }

        dsl.exe {
            name 'test'
            version '0.0.1'

        }

    }



}
