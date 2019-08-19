package org.ahocorasick.trie;

/**
 * PayloadMatchToken holds a text ("the fragment") an emits some output.
 * <p>
 * It matches a search term - so its <code>isMatch</code>-method returns always
 * true..
 * 
 * @author Daniel Beck
 *
 * @param <T> The Type of the emitted payloads.
 */
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
