package org.ahocorasick.trie;

import java.util.regex.Pattern;

public class TrieConfig {

    private boolean allowOverlaps = true;

    private boolean onlyWholeWords = false;

    private boolean caseInsensitive = false;

    private Pattern ignorePattern = null;

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

    public void setIgnorePattern(Pattern ignorePattern) {
		this.ignorePattern = ignorePattern;
	}

    public Pattern getIgnorePattern() {
		return ignorePattern;
	}
}
