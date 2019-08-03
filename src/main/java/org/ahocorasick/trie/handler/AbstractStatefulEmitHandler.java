package org.ahocorasick.trie.handler;

import java.util.ArrayList;
import java.util.List;

import org.ahocorasick.trie.Emit;

public abstract class AbstractStatefulEmitHandler<T> implements StatefulEmitHandler<T> {

    private final List<Emit<T>> emits = new ArrayList<>();

    public void addEmit(final Emit<T> emit) {
        this.emits.add(emit);
    }

    @Override
    public List<Emit<T>> getEmits() {
        return this.emits;
    }

}
