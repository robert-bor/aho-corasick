package org.ahocorasick.trie.handler;

import org.ahocorasick.trie.PayloadEmit;

public interface PayloadEmitHandler<T> {
    boolean emit(PayloadEmit<T> emit);
}
