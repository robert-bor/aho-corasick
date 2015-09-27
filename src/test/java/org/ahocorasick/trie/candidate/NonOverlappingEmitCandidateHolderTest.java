package org.ahocorasick.trie.candidate;

import org.ahocorasick.trie.Emit;
import org.junit.Test;

import java.util.Collection;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class NonOverlappingEmitCandidateHolderTest {

    @Test
    public void retainLongestEmit() {
        EmitCandidateHolder holder = new NonOverlappingEmitCandidateHolder();
        holder.addCandidate(new Emit(0, 2, "she"));
        holder.addCandidate(new Emit(1, 2, "he"));
        List<Emit> emits = holder.flush();
        assertEquals(1, emits.size());
        assertEquals("she", emits.get(0).getKeyword());
    }

    @Test
    public void multipleOverlaps() {
        EmitCandidateHolder holder = new NonOverlappingEmitCandidateHolder();
        holder.addCandidate(new Emit(0, 4, "ababc"));
        holder.addCandidate(new Emit(4, 6, "cba"));
        holder.addCandidate(new Emit(6, 7, "ab"));
        List<Emit> emits = holder.flush();
        assertEquals(2, emits.size());
        assertEquals("ababc", emits.get(0).getKeyword());
        assertEquals("ab", emits.get(1).getKeyword());
    }

}
