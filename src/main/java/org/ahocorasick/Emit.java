package org.ahocorasick;

public class Emit {
    private final int start;
    private final int end;
    private final String keyword;

    public Emit(final int start, final int end, final String keyword) {
        this.start = start;
        this.end = end;
        this.keyword = keyword;
    }

    public int getStart() {
        return this.start;
    }

    public int getEnd() {
        return this.end;
    }

    public String getKeyword() {
        return this.keyword;
    }

}
