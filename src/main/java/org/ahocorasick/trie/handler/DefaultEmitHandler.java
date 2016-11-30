package org.ahocorasick.trie.handler;

import org.ahocorasick.trie.Emit;

import java.util.ArrayList;
import java.util.List;

public class DefaultEmitHandler implements EmitHandler {

    private final List<Emit> emits = new ArrayList<>();

    @Override
    public void emit(final Emit emit) {
        this.emits.add(emit);
    }

    public List<Emit> getEmits() {
        return this.emits;
    }
}
