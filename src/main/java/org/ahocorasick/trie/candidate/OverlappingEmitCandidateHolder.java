package org.ahocorasick.trie.candidate;

import org.ahocorasick.trie.Emit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class OverlappingEmitCandidateHolder implements EmitCandidateHolder {

    protected List<Emit> emitCandidates = new ArrayList<>();

    @Override
    public void addCandidate(Emit emitCandidate) {
        this.emitCandidates.add(emitCandidate);
    }

    @Override
    public List<Emit> flush() {
        return reset(emitCandidates);
    }

    private List<Emit> reset(List<Emit> emitCandidates) {
        this.emitCandidates = new ArrayList<>();
        return emitCandidates;
    }

}
