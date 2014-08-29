package org.ahocorasick.trie;

import java.io.IOException;
import java.io.Serializable;

public class TrieConfig implements Serializable, Comparable<TrieConfig> {

    private boolean allowOverlaps = true;

    private boolean onlyWholeWords = false;

    private boolean caseInsensitive = false;

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

    private void writeObject(java.io.ObjectOutputStream stream)
            throws IOException {
        stream.writeBoolean(allowOverlaps);
        stream.writeBoolean(onlyWholeWords);
        stream.writeBoolean(caseInsensitive);
    }

    private void readObject(java.io.ObjectInputStream stream)
            throws IOException, ClassNotFoundException {
        this.allowOverlaps = stream.readBoolean();
        this.onlyWholeWords = stream.readBoolean();
        this.caseInsensitive = stream.readBoolean();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof TrieConfig))
            return false;
        return compareTo((TrieConfig) obj) == 0;
    }

    @Override
    public int compareTo(TrieConfig o) {
        if (this.allowOverlaps != o.allowOverlaps || this.caseInsensitive != o.caseInsensitive ||
                this.onlyWholeWords != o.onlyWholeWords) {
            return 1;
        } else {
            return 0;
        }
    }
}
