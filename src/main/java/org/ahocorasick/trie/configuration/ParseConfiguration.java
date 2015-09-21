package org.ahocorasick.trie.configuration;

import org.ahocorasick.trie.handler.EmitHandler;

import java.io.Reader;
import java.util.Iterator;

public class ParseConfiguration implements Iterable<Character> {

    private String text;

    private Reader reader;

    private EmitHandler emitHandler;

    public ParseConfiguration setText(String text) {
        this.text = text;
        return this;
    }

    public ParseConfiguration setText(Reader reader) {
        this.reader = reader;
        return this;
    }

    public ParseConfiguration setEmitHandler(EmitHandler emitHandler) {
        this.emitHandler = emitHandler;
        return this;
    }

    public EmitHandler getEmitHandler() {
        return emitHandler;
    }

    @Override
    public Iterator<Character> iterator() {
        if (reader != null) {
            return new ReaderIterator(reader);
        }
        return new StringIterator(text);
    }

}
