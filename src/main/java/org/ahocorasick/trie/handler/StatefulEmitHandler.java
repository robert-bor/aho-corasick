package org.ahocorasick.trie.handler;

import java.util.List;

import org.ahocorasick.trie.Emit;

public interface StatefulEmitHandler<T> extends EmitHandler<T> {
    List<Emit<T>> getEmits();
}
