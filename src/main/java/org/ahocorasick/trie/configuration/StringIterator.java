package org.ahocorasick.trie.configuration;

import java.util.Iterator;

public class StringIterator implements Iterator<Character> {

    private int counter = 0;

    private String text;

    public StringIterator(String text) {
        this.text = text;
    }

    @Override
    public boolean hasNext() {
        return counter < text.length();
    }

    @Override
    public Character next() {
        return text.charAt(counter++);
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

}
