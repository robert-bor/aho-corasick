package org.ahocorasick.interval;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class IntervalNode {

    private enum Direction { LEFT, RIGHT }

    private IntervalNode left = null;
    private IntervalNode right = null;
    private final int point;
    private final List<Intervalable> intervals = new ArrayList<>();

    public IntervalNode(List<Intervalable> intervals) {
        this.point = determineMedian(intervals);

        List<Intervalable> toLeft = new ArrayList<>();
        List<Intervalable> toRight = new ArrayList<>();

        for (Intervalable interval : intervals) {
            if (interval.getEnd() < this.point) {
                toLeft.add(interval);
            } else if (interval.getStart() > this.point) {
                toRight.add(interval);
            } else {
                this.intervals.add(interval);
            }
        }

        if (toLeft.size() > 0) {
            this.left = new IntervalNode(toLeft);
        }
        if (toRight.size() > 0) {
            this.right = new IntervalNode(toRight);
        }
    }

    private int determineMedian(List<Intervalable> intervals) {
        int start = -1;
        int end = -1;
        for (Intervalable interval : intervals) {
            int currentStart = interval.getStart();
            int currentEnd = interval.getEnd();
            if (start == -1 || currentStart < start) {
                start = currentStart;
            }
            if (end == -1 || currentEnd > end) {
                end = currentEnd;
            }
        }
        return (start + end) / 2;
    }

    public List<Intervalable> findOverlaps(Intervalable interval) {

        List<Intervalable> overlaps = new ArrayList<>();

        if (this.point < interval.getStart()) { // Tends to the right
            addToOverlaps(interval, overlaps, findOverlappingRanges(this.right, interval));
            addToOverlaps(interval, overlaps, checkForOverlapsToTheRight(interval));
        } else if (this.point > interval.getEnd()) { // Tends to the left
            addToOverlaps(interval, overlaps, findOverlappingRanges(this.left, interval));
            addToOverlaps(interval, overlaps, checkForOverlapsToTheLeft(interval));
        } else { // Somewhere in the middle
            addToOverlaps(interval, overlaps, this.intervals);
            addToOverlaps(interval, overlaps, findOverlappingRanges(this.left, interval));
            addToOverlaps(interval, overlaps, findOverlappingRanges(this.right, interval));
        }

        return overlaps;
    }

    private void addToOverlaps(Intervalable interval, List<Intervalable> overlaps, List<Intervalable> newOverlaps) {
        overlaps.addAll(newOverlaps.stream().filter(currentInterval -> !currentInterval.equals(interval)).collect(Collectors.toList()));
    }

    private List<Intervalable> checkForOverlapsToTheLeft(Intervalable interval) {
        return checkForOverlaps(interval, Direction.LEFT);
    }

    private List<Intervalable> checkForOverlapsToTheRight(Intervalable interval) {
        return checkForOverlaps(interval, Direction.RIGHT);
    }

    private List<Intervalable> checkForOverlaps(Intervalable interval, Direction direction) {

        List<Intervalable> overlaps = new ArrayList<>();
        for (Intervalable currentInterval : this.intervals) {
            switch (direction) {
                case LEFT :
                    if (currentInterval.getStart() <= interval.getEnd()) {
                        overlaps.add(currentInterval);
                    }
                    break;
                case RIGHT :
                    if (currentInterval.getEnd() >= interval.getStart()) {
                        overlaps.add(currentInterval);
                    }
                    break;
            }
        }
        return overlaps;
    }


    private List<Intervalable> findOverlappingRanges(IntervalNode node, Intervalable interval) {
        if (node != null) {
            return node.findOverlaps(interval);
        }
        return Collections.emptyList();
    }

}
