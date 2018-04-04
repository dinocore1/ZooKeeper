package com.devsmart.zookeeper.projectmodel

import org.junit.Test
import static org.junit.Assert.*

class BuildableModuleTest {

    @Test
    void testSetDependencies() {
        BuildableModule module = new BuildableModule()

        module.dependencies({
            lib 'foo:0.0.1'
            lib 'bar:0.1.0'
        })

        Set<Library> libs = module.dependencies
        assertNotNull(libs)
        assertEquals(2, libs.size())
    }
}
