package org.ahocorasick.trie.handler;

import org.ahocorasick.trie.Emit;

import java.util.ArrayList;
import java.util.List;

public class DefaultEmitHandler<T> implements StatefulEmitHandler<T> {

    private final List<Emit<T>> emits = new ArrayList<>();

    @Override
    public boolean emit(final Emit<T> emit) {
        this.emits.add(emit);
        return true;
    }

    @Override
    public List<Emit<T>> getEmits() {
        return this.emits;
    }
}
