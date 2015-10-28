package org.ahocorasick.trie;

import org.ahocorasick.interval.Interval;
import org.ahocorasick.interval.Intervalable;

public class Emit extends Interval implements Intervalable {

    private final String keyword;
    private final boolean isWholeWord;

    public Emit(final int start, final int end, 
            final String keyword, boolean isWholeWord) {
        super(start, end);
        this.keyword = keyword;
        this.isWholeWord = isWholeWord;
    }
    
    public Emit(final int start, final int end, final String keyword) {
        this(start, end, keyword, true);
    }

    public String getKeyword() {
        return this.keyword;
    }
    
    public boolean isWholeWord() {
        return isWholeWord;
    }

    @Override
    public String toString() {
        return super.toString() + "=" + this.keyword;
    }

}
