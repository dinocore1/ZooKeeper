package com.devsmart.zookeeper

import org.junit.Test
import static org.junit.Assert.*

class ZooKeeperDSLTest {

    @Test
    void testAssembleTemplate() {

        ZooKeeperDSL dsl = new ZooKeeperDSL(){

            @Override
            Object run() {
                return null
            }
        }
        dsl.compile {
            flags '-std=c++11'

            debug {
                flags '-Wall', '-g', '-O0'
            }

            release {
                flags '-O3'
            }

            sharedlib {
                flags '-fPIC'
            }

            cmd 'c++', flags, prefix('-I', includes), ['-o', output], ['-c', input]
        }


        assertNotNull dsl.getProperty("compileBuilder")
        CompileTemplate template = dsl.getProperty("compileBuilder").buildTemplate(CompileTemplateBuilder.Variants.exe, CompileTemplateBuilder.Variants.debug)
        assertNotNull template


    }
}
