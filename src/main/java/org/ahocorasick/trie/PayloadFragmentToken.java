package org.ahocorasick.trie;

/***
 * PayloadFragmentToken holds a text ("the fragment").
 * <p>
 * It does not matches a search term - so its <code>isMatch</code>-method
 * returns always false. <code>getEmits</code> returns not Emits.
 * 
 * @author Daniel Beck
 *
 * @param <T> The Type of the emitted payloads.
 */
public class PayloadFragmentToken<T> extends PayloadToken<T> {

    public PayloadFragmentToken(String fragment) {
        super(fragment);
    }

    @Override
    public boolean isMatch() {
        return false;
    }

    /**
     * Returns null.
     */
    @Override
    public PayloadEmit<T> getEmit() {
        return null;
    }
}
