package com.devsmart.zookeeper;

import org.junit.Test;
import static org.junit.Assert.*;

public class StringContextTest {

    @Test
    public void testStringReplace() {
        StringContext env = new StringContext();
        env.setVar("PATH", "this/is/my/new/path");
        env.setVar("something", "this is not used");

        assertEquals("cool/dudes/this/is/my/new/path", env.resolve("cool/dudes/$(PATH)"));
    }
}
