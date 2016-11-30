package org.ahocorasick.interval;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.sort;
import static junit.framework.Assert.assertEquals;

public class IntervalableComparatorByPositionTest {

    @Test
    public void sortOnPosition() {
        List<Intervalable> intervals = new ArrayList<>();
        intervals.add(new Interval(4, 5));
        intervals.add(new Interval(1, 4));
        intervals.add(new Interval(3, 8));
        sort(intervals, new IntervalableComparatorByPosition());
        assertEquals(4, intervals.get(0).size());
        assertEquals(6, intervals.get(1).size());
        assertEquals(2, intervals.get(2).size());
    }

}
