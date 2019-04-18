package org.ahocorasick.trie;

public class TrieConfig {

    private boolean allowOverlaps = true;

    private boolean onlyWholeWords = false;

    private boolean onlyWholeWordsWhiteSpaceSeparated = false;

    private boolean caseInsensitive = false;

    private boolean stopOnHit = false;

    /**
     * allow the failure-jump when current not match
     */
    private boolean allowFailureJump = false;

    public boolean isStopOnHit() {
        return stopOnHit;
    }

    public void setStopOnHit(boolean stopOnHit) {
        this.stopOnHit = stopOnHit;
    }

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

    public boolean isOnlyWholeWordsWhiteSpaceSeparated() {
        return onlyWholeWordsWhiteSpaceSeparated;
    }

    public void setOnlyWholeWordsWhiteSpaceSeparated(boolean onlyWholeWordsWhiteSpaceSeparated) {
        this.onlyWholeWordsWhiteSpaceSeparated = onlyWholeWordsWhiteSpaceSeparated;
    }

    public boolean isCaseInsensitive() {
        return caseInsensitive;
    }

    public void setCaseInsensitive(boolean caseInsensitive) {
        this.caseInsensitive = caseInsensitive;
    }

    public boolean isAllowFailureJump() {
        return allowFailureJump;
    }

    public void setAllowFailureJump(boolean allowFailureJump) {
        this.allowFailureJump = allowFailureJump;
    }
}
