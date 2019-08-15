package org.ahocorasick.trie.handler;

import java.util.List;

import org.ahocorasick.trie.PayloadEmit;

public interface StatefulPayloadEmitHandler<T> extends PayloadEmitHandler<T>{
    List<PayloadEmit<T>> getEmits();
}
