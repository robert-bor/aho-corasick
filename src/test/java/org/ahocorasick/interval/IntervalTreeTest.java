package org.ahocorasick.interval;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static junit.framework.Assert.assertEquals;

public class IntervalTreeTest {
    
    @Test
    public void findOverlaps() {
        List<Intervalable> intervals = new ArrayList<Intervalable>();
        intervals.add(new Interval(0, 2));
        intervals.add(new Interval(1, 3));
        intervals.add(new Interval(2, 4));
        intervals.add(new Interval(3, 5));
        intervals.add(new Interval(4, 6));
        intervals.add(new Interval(5, 7));
        IntervalTree intervalTree = new IntervalTree(intervals);
        List<Intervalable> overlaps = intervalTree.findOverlaps(new Interval(1,3));
        assertEquals(3, overlaps.size());
        Iterator<Intervalable> overlapsIt = overlaps.iterator();
        assertOverlap(overlapsIt.next(), 2, 4);
        assertOverlap(overlapsIt.next(), 3, 5);
        assertOverlap(overlapsIt.next(), 0, 2);
    }

    @Test
    public void removeOverlaps() {
        List<Intervalable> intervals = new ArrayList<Intervalable>();
        intervals.add(new Interval(0, 2));
        intervals.add(new Interval(4, 5));
        intervals.add(new Interval(2, 10));
        intervals.add(new Interval(6, 13));
        intervals.add(new Interval(9, 15));
        intervals.add(new Interval(12, 16));
        IntervalTree intervalTree = new IntervalTree(intervals);
        intervals = intervalTree.removeOverlaps(intervals);
        assertEquals(2, intervals.size());

    }

    protected void assertOverlap(Intervalable interval, int expectedStart, int expectedEnd) {
        assertEquals(expectedStart, interval.getStart());
        assertEquals(expectedEnd, interval.getEnd());
    }
    
}
