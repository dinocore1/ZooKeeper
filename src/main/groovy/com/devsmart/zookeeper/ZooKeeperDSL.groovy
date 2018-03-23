package com.devsmart.zookeeper

abstract class ZooKeeperDSL extends Script {

    def compile(Closure cl) {
        compileBuilder = CompileTemplateBuilder.make(cl)
    }

    def link(Closure cl) {
        linkBuilder = CompileTemplateBuilder.make(cl)
    }

}