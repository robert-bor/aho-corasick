package org.ahocorasick.interval;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class IntervalTree {

    private final IntervalNode rootNode;

    public IntervalTree(List<Intervalable> intervals) {
        this.rootNode = new IntervalNode(intervals);
    }

    public List<Intervalable> removeOverlaps(final List<Intervalable> intervals) {
        if (intervals.isEmpty()) {
            return intervals;
        }

        // Sort the intervals on size, then left-most position
        intervals.sort(IntervalableComparatorBySize.INSTANCE);

        final Set<Intervalable> removeIntervals = new TreeSet<>();

        for (final Intervalable interval : intervals) {
            // If the interval was already removed, ignore it
            if (removeIntervals.contains(interval)) {
                continue;
            }

            // Remove all overallping intervals
            removeIntervals.addAll(findOverlaps(interval));
        }

        // Remove all intervals that were overlapping
        intervals.removeAll(removeIntervals);

        // Sort the intervals, now on left-most position only
        intervals.sort(IntervalableComparatorByPosition.INSTANCE);

        return intervals;
    }

    public List<Intervalable> findOverlaps(final Intervalable interval) {
        return rootNode.findOverlaps(interval);
    }

}
