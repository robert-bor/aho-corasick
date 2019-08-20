package org.ahocorasick.trie;

/***
 * PayloadToken holds a text ("the fragment") an emits some output. If
 * <code>isMatch</code> returns true, the token matched a search term.
 * 
 * @author Daniel Beck
 *
 * @param <T> The Type of the emitted payloads.
 */
public abstract class PayloadToken<T> {
    private String fragment;

    public PayloadToken(String fragment) {
        this.fragment = fragment;
    }

    public String getFragment() {
        return this.fragment;
    }

    /**
     * Return true if a search term matched.
     */
    public abstract boolean isMatch();

    public abstract PayloadEmit<T> getEmit();
}
