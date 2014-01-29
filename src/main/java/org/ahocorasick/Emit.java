package org.ahocorasick;

public class Emit {
    private final int position;
    private final String keyword;

    public Emit(final int position, final String keyword) {
        this.position = position;
        this.keyword = keyword;
    }

    public int getPosition() {
        return this.position;
    }

    public String getKeyword() {
        return this.keyword;
    }

}
