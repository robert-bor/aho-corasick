package org.ahocorasick.trie.handler;

import java.util.List;

import org.ahocorasick.trie.Emit;

public interface StatefulEmitHandler extends EmitHandler {
    List<Emit> getEmits();
}
