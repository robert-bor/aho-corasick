package org.ahocorasick.trie.handler;

import org.ahocorasick.trie.Emit;

import java.util.ArrayList;
import java.util.List;

public class DefaultEmitHandler extends SimpleEmitHandler {

    private List<Emit> emits = new ArrayList<>();

    @Override
    public void emit(Emit emit) {
        this.emits.add(emit);
    }

    public List<Emit> getEmits() {
        return this.emits;
    }

}
