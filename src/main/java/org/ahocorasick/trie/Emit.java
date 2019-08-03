package org.ahocorasick.trie;

import org.ahocorasick.interval.Interval;
import org.ahocorasick.interval.Intervalable;

public class Emit<T> extends Interval implements Intervalable {

    private final Payload<T> payload;

    public Emit(final int start, final int end, final Payload<T> payload) {
        super(start, end);
        this.payload = payload;
    }

    public String getKeyword() {
        return this.payload.getKeyword();
    }

    public Payload<T> getPayload() {
        return payload;
    }

    @Override
    public String toString() {
        return super.toString() + "=" + this.payload.getKeyword();
    }
}
