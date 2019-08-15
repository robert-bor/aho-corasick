package org.ahocorasick.trie;

public class PayloadMatchToken<T> extends PayloadToken<T> {

    private final PayloadEmit<T> emit;

    public PayloadMatchToken(final String fragment, final PayloadEmit<T> emit) {
        super(fragment);
        this.emit = emit;
    }

    @Override
    public boolean isMatch() {
        return true;
    }

    @Override
    public PayloadEmit<T> getEmit() {
        return this.emit;
    }
}
