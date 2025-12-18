package org.ahocorasick.interval;

import org.junit.Test;

import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import static org.junit.Assert.*;

public class IntervalTest {

    @Test
    public void test_construct() {
        final Interval i = new Interval(1, 3);
        assertEquals(1, i.getStart());
        assertEquals(3, i.getEnd());
    }

    @Test
    public void test_size() {
        assertEquals(3, new Interval(0, 2).size());
    }

    @Test
    public void test_intervaloverlaps() {
        assertTrue(new Interval(1, 3).overlapsWith(new Interval(2, 4)));
    }

    @Test
    public void test_intervalDoesNotOverlap() {
        assertFalse(new Interval(1, 13).overlapsWith(new Interval(27, 42)));
    }

    @Test
    public void test_pointOverlaps() {
        assertTrue(new Interval(1, 3).overlapsWith(2));
    }

    @Test
    public void test_pointDoesNotOverlap() {
        assertFalse(new Interval(1, 13).overlapsWith(42));
    }

    @Test
    public void test_comparable() {
        final Set<Interval> intervals = new TreeSet<>();
        intervals.add(new Interval(4, 6));
        intervals.add(new Interval(2, 7));
        intervals.add(new Interval(3, 4));
        final Iterator<Interval> it = intervals.iterator();
        assertEquals(2, it.next().getStart());
        assertEquals(3, it.next().getStart());
        assertEquals(4, it.next().getStart());
    }

    @Test
    public void test_checkToString() {
        assertEquals("4:6", new Interval(4, 6).toString());
    }

    @Test
    public void test_compareToNegativeTest() {
        assertEquals(-1, new Interval(4, 6).compareTo(new Object()));
    }

}
