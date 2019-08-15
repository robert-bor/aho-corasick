package org.ahocorasick.trie;

import org.ahocorasick.interval.Interval;
import org.ahocorasick.interval.Intervalable;

public class PayloadEmit<T> extends Interval implements Intervalable {

    private final String keyword;

    private final T payload;

    public PayloadEmit(final int start, final int end, String keyword, T payload) {
        super(start, end);
        this.keyword = keyword;
        this.payload = payload;
    }

    public String getKeyword() {
        return this.keyword;
    }

    public T getPayload() {
        return this.payload;
    }

    @Override
    public String toString() {
        return super.toString() + "=" + this.keyword + (this.payload != null ? "->" + this.payload : "");
    }
}
