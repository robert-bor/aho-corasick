package org.ahocorasick.trie;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

/**
 * Test the {@link Emit} class functionality.
 */
public class EmitTest {

    /**
     * Test that two {@link Emit} instances having the same values are equal.
     */
    @Test
    public void test_Equality_SameValues_ObjectsAreEqual() {
        final Emit one = new Emit(13, 42, null);
        final Emit two = new Emit(13, 42, null);
        assertEquals( one, two );
    }

    /**
     * Test that two {@link Emit} instances having different values are equal.
     */
    @Test
    public void test_Equality_DifferingValues_ObjectsAreNotEqual() {
        final Emit one = new Emit(13, 42, null);
        final Emit two = new Emit(13, 43, null);
        assertNotEquals(one, two);
    }
}
