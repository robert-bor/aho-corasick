package org.ahocorasick.trie;

public class TrieConfig {

    private boolean allowOverlaps = true;

    private boolean onlyWholeWords = false;

    private boolean caseInsensitive = false;
    
    private boolean wordTransitions = false;

    public boolean isAllowOverlaps() {
        return allowOverlaps;
    }

    public void setAllowOverlaps(boolean allowOverlaps) {
        this.allowOverlaps = allowOverlaps;
    }

    public boolean isOnlyWholeWords() {
        return onlyWholeWords;
    }

    public void setOnlyWholeWords(boolean onlyWholeWords) {
        this.onlyWholeWords = onlyWholeWords;
    }

    public boolean isCaseInsensitive() {
        return caseInsensitive;
    }

    public void setCaseInsensitive(boolean caseInsensitive) {
        this.caseInsensitive = caseInsensitive;
    }
    
    public boolean hasWordTransitions() {
        return wordTransitions;
    }
    
    public void setWordTransitions(boolean wordNodes) {
        this.wordTransitions = wordNodes;
    }
}
