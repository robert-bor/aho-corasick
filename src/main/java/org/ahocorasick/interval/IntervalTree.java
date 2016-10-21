package org.ahocorasick.interval;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * The tree of intervals.
 */
public class IntervalTree {

    /**
     * The root node.
     */
    private IntervalNode rootNode = null;

    /**
     * The constructor splits the intervals on the median value.
     * @param intervals the intervals.
     */
    public IntervalTree(List<? extends Intervalable> intervals) {
        this.rootNode = new IntervalNode(intervals);
    }

    /**
     * Removes overlaps from the given list of intervals and returns the same collection.
     * @param intervals the list of intervals.
     * @return the original list of intervals.
     */
    public List<? extends Intervalable> removeOverlaps(List<? extends Intervalable> intervals) {

        // Sort the intervals on size, then left-most position
        Collections.sort(intervals, new IntervalableComparatorBySize());

        Set<Intervalable> removeIntervals = new TreeSet<Intervalable>();

        for (Intervalable interval : intervals) {
            // If the interval was already removed, ignore it
            if (removeIntervals.contains(interval)) {
                continue;
            }

            // Remove all overlapping intervals
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

    /**
     * Finds the list of intervals that overlap the given interval.
     * @param interval the interval to match against.
     * @return the list of intervals that overlap the given interval.
     */
    public List<Intervalable> findOverlaps(Intervalable interval) {
        return rootNode.findOverlaps(interval);
    }

}
