package org.ahocorasick.trie;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotSame;

public class EmitTest {

    @Test
    public void equals() {
        Emit<String> one = new Emit<>(13, 42, null);
        Emit<String> two = new Emit<>(13, 42, null);
        assertEquals(one, two);
    }

    @Test
    public void notEquals() {
        Emit<String> one = new Emit<>(13, 42, null);
        Emit<String> two = new Emit<>(13, 43, null);
        assertNotSame(one, two);
    }

}
