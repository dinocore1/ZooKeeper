package com.devsmart.zookeeper

abstract class ZooKeeperDSL extends Script {

    def compile(Closure cl) {
        def builder = CompileTemplateBuilder.make(cl)
        templates.add(builder)
    }

}
