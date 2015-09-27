package org.ahocorasick.trie.candidate;

import org.ahocorasick.interval.IntervalTree;
import org.ahocorasick.interval.Intervalable;
import org.ahocorasick.trie.Emit;

import java.util.*;

public class NonOverlappingEmitCandidateHolder extends OverlappingEmitCandidateHolder {

    @Override
    public List<Emit> flush() {
        IntervalTree intervalTree = new IntervalTree((List<Intervalable>)(List<?>)emitCandidates);
        intervalTree.removeOverlaps((List<Intervalable>) (List<?>) emitCandidates);
        return super.flush();
    }

}
