package org.ahocorasick.trie;

public class MatchToken extends Token {

    private final boolean wholeWord;

    private final Emit emit;

    public MatchToken(String fragment, Emit emit, boolean wholeWord) {
        super(fragment);
        this.emit = emit;
        this.wholeWord = wholeWord;
    }

    @Override
    public boolean isWholeWord() {
        return wholeWord;
    }

    @Override
    public boolean isMatch() {
        return true;
    }

    @Override
    public Emit getEmit() {
        return this.emit;
    }

}
