package org.ahocorasick.trie.candidate;

import org.ahocorasick.trie.Emit;

import java.util.List;

public interface EmitCandidateHolder {

    void addCandidate(Emit emitCandidate);
    List<Emit> flush();

}
