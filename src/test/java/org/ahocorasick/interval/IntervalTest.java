package org.ahocorasick.interval;

import org.junit.Test;

import java.util.*;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

public class IntervalTest {

    @Test
    public void construct() {
        Interval i = new Interval(1, 3);
        assertEquals(1, i.getStart());
        assertEquals(3, i.getEnd());
    }

    @Test
    public void size() {
        assertEquals(3, new Interval(0, 2).size());
    }

    @Test
    public void intervaloverlaps() {
        assertTrue(new Interval(1, 3).overlapsWith(new Interval(2, 4)));
    }

    @Test
    public void intervalDoesNotOverlap() {
        assertFalse(new Interval(1, 13).overlapsWith(new Interval(27, 42)));
    }

    @Test
    public void pointOverlaps() {
        assertTrue(new Interval(1, 3).overlapsWith(2));
    }

    @Test
    public void pointDoesNotOverlap() {
        assertFalse(new Interval(1, 13).overlapsWith(42));
    }

    @Test
    public void comparable() {
        Set<Interval> intervals = new TreeSet<Interval>();
        intervals.add(new Interval(4, 6));
        intervals.add(new Interval(2, 7));
        intervals.add(new Interval(3, 4));
        Iterator<Interval> it = intervals.iterator();
        assertEquals(2, it.next().getStart());
        assertEquals(3, it.next().getStart());
        assertEquals(4, it.next().getStart());
    }

}
