package org.ahocorasick.trie;

public class FragmentToken extends Token {

    private boolean whiteSpace;

    public FragmentToken(String fragment) {
        super(fragment);
        this.whiteSpace = true;
        for (int position = 0; position < fragment.length(); position++) {
            if (!Character.isWhitespace(fragment.charAt(position))) {
                whiteSpace = false;
            }
        }
    }

    @Override
    public boolean isMatch() {
        return false;
    }

    @Override
    public Emit getEmit() {
        return null;
    }

    @Override
    public boolean isWhiteSpace() {
        return whiteSpace;
    }

}
