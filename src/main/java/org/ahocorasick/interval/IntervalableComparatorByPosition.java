package org.ahocorasick.interval;

import java.util.Comparator;

public final class IntervalableComparatorByPosition implements Comparator<Intervalable> {

    private IntervalableComparatorByPosition() {}

    public static final IntervalableComparatorByPosition INSTANCE = new IntervalableComparatorByPosition();

    @Override
    public int compare(final Intervalable intervalable, final Intervalable intervalable2) {
        return intervalable.getStart() - intervalable2.getStart();
    }

}
