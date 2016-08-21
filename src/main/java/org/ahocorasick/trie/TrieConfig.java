package org.ahocorasick.trie;

import java.io.IOException;
import java.io.Serializable;

public class TrieConfig implements Serializable {

    private boolean allowOverlaps = true;

    private boolean onlyWholeWords = false;

    private boolean onlyWholeWordsWhiteSpaceSeparated = false;

    private boolean caseInsensitive = false;

    private boolean stopOnHit = false;

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

    private void writeObject(java.io.ObjectOutputStream stream)
            throws IOException {
        stream.writeBoolean(allowOverlaps);
        stream.writeBoolean(onlyWholeWords);
        stream.writeBoolean(onlyWholeWordsWhiteSpaceSeparated);
        stream.writeBoolean(caseInsensitive);
        stream.writeBoolean(stopOnHit);
    }

    private void readObject(java.io.ObjectInputStream stream)
            throws IOException, ClassNotFoundException {
        this.allowOverlaps = stream.readBoolean();
        this.onlyWholeWords = stream.readBoolean();
        onlyWholeWordsWhiteSpaceSeparated = stream.readBoolean();
        this.caseInsensitive = stream.readBoolean();
        this.stopOnHit = stream.readBoolean();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TrieConfig that = (TrieConfig) o;
        return allowOverlaps == that.allowOverlaps &&
                onlyWholeWords == that.onlyWholeWords &&
                onlyWholeWordsWhiteSpaceSeparated == that.onlyWholeWordsWhiteSpaceSeparated &&
                caseInsensitive == that.caseInsensitive &&
                stopOnHit == that.stopOnHit;
    }
}
