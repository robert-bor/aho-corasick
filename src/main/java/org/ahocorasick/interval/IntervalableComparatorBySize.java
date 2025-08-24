package org.ahocorasick.interval;

import java.util.Comparator;

public final class IntervalableComparatorBySize implements Comparator<Intervalable> {

    private IntervalableComparatorBySize() {}

    public static final IntervalableComparatorBySize INSTANCE = new IntervalableComparatorBySize();

    @Override
    public int compare(final Intervalable intervalable, final Intervalable intervalable2) {
        int comparison = intervalable2.size() - intervalable.size();

        if (comparison == 0) {
            comparison = intervalable.getStart() - intervalable2.getStart();
        }

        return comparison;
    }

}
