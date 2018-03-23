package com.devsmart.zookeeper

abstract class ZooKeeperDSL extends Script {

    def name(String n) {
        name = n
    }

    def language(String lang) {
        language = lang
    }

    def compile(Closure cl) {
        compileBuilder = CompileTemplateBuilder.make(cl)
    }

    def link(Closure cl) {
        linkBuilder = CompileTemplateBuilder.make(cl)
    }

}