package org.ahocorasick.trie.configuration;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.Iterator;

public class ReaderIterator implements Iterator<Character> {

    private Reader reader;

    private int readCharacter;

    public ReaderIterator(Reader reader) {
        this.reader = reader;
        readIntoBuffer();
    }

    private void readIntoBuffer() {
        try {
            readCharacter = reader.read();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean hasNext() {
        return readCharacter != -1;
    }

    @Override
    public Character next() {
        Character returnChar = (char)readCharacter;
        readIntoBuffer();
        return returnChar;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

}
