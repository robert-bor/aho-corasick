package org.ahocorasick.trie;

public abstract class PayloadToken<T> {
    private String fragment;

    public PayloadToken(String fragment) {
        this.fragment = fragment;
    }

    public String getFragment() {
        return this.fragment;
    }

    public abstract boolean isMatch();

    public abstract PayloadEmit<T> getEmit();
}
