package org.ahocorasick.trie.handler;

import org.ahocorasick.trie.Emit;

public abstract class SimpleEmitHandler implements EmitHandler {

    @Override
    public abstract void emit(Emit emit);

    @Override
    public boolean stop() {
        return false;
    }
}
