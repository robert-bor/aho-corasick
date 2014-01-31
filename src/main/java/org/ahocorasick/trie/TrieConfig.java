package org.ahocorasick.trie;

public class TrieConfig {

    private boolean allowOverlaps = true;

    public boolean isAllowOverlaps() {
        return allowOverlaps;
    }

    public void setAllowOverlaps(boolean allowOverlaps) {
        this.allowOverlaps = allowOverlaps;
    }

}
