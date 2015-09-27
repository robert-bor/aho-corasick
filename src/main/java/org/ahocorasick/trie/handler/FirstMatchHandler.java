package org.ahocorasick.trie.handler;

import org.ahocorasick.trie.Emit;

public class FirstMatchHandler extends SimpleEmitHandler {

    private Emit firstMatch;

    private boolean stop = false;

    @Override
    public void emit(Emit emit) {
        if (!stop) {
            firstMatch = emit;
            stop = true;
        }
    }

    public Emit getFirstMatch() {
        return firstMatch;
    }

    @Override
    public boolean stop() {
        return this.stop;
    }

}
