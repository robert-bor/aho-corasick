package org.ahocorasick.trie;

public class PayloadFragmentToken<T> extends PayloadToken<T> {

    public PayloadFragmentToken(String fragment) {
        super(fragment);
    }

    @Override
    public boolean isMatch() {
        return false;
    }

    @Override
    public PayloadEmit<T> getEmit() {
        return null;
    }
}
