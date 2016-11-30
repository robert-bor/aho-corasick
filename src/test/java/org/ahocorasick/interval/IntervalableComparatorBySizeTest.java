package org.ahocorasick.interval;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static junit.framework.Assert.assertEquals;

public class IntervalableComparatorBySizeTest {

    @Test
    public void sortOnSize() {
        List<Intervalable> intervals = new ArrayList<Intervalable>();
        intervals.add(new Interval(4, 5));
        intervals.add(new Interval(1, 4));
        intervals.add(new Interval(3, 8));
        Collections.sort(intervals, new IntervalableComparatorBySize());
        assertEquals(6, intervals.get(0).size());
        assertEquals(4, intervals.get(1).size());
        assertEquals(2, intervals.get(2).size());
    }

    @Test
    public void sortOnSizeThenPosition() {
        List<Intervalable> intervals = new ArrayList<Intervalable>();
        intervals.add(new Interval(4, 7));
        intervals.add(new Interval(2, 5));
        Collections.sort(intervals, new IntervalableComparatorBySize());
        assertEquals(2, intervals.get(0).getStart());
        assertEquals(4, intervals.get(1).getStart());
    }

}
