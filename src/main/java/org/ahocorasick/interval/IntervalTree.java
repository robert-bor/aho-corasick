package org.ahocorasick.interval;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class IntervalTree {

    private IntervalNode rootNode = null;

    public IntervalTree(List<Intervalable> intervals) {
        this.rootNode = new IntervalNode(intervals);
    }

    public List<Intervalable> removeOverlaps(List<Intervalable> intervals) {

        // Sort the intervals on size, then left-most position
        Collections.sort(intervals, new IntervalableComparatorBySize());

        Set<Intervalable> removeIntervals = new TreeSet<Intervalable>();

        for (Intervalable interval : intervals) {
            // If the interval was already removed, ignore it
            if (removeIntervals.contains(interval)) {
                continue;
            }

            // Remove all overallping intervals
            removeIntervals.addAll(findOverlaps(interval));
        }

        // Remove all intervals that were overlapping
        for (Intervalable removeInterval : removeIntervals) {
            intervals.remove(removeInterval);
        }

        // Sort the intervals, now on left-most position only
        Collections.sort(intervals, new IntervalableComparatorByPosition());

        return intervals;
    }

    public List<Intervalable> findOverlaps(Intervalable interval) {
        return rootNode.findOverlaps(interval);
    }

}
