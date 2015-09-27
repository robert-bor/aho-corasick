package org.ahocorasick.trie.candidate;

import org.ahocorasick.trie.Emit;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class OverlappingEmitCandidateHolderTest {

    @Test
    public void addAndFlush() {
        EmitCandidateHolder holder = new OverlappingEmitCandidateHolder();
        holder.addCandidate(new Emit(0, 2, "ABC"));
        holder.addCandidate(new Emit(2, 4, "CDE"));
        List<Emit> emits = holder.flush();
        assertEquals(2, emits.size());
        assertEquals("ABC", emits.get(0).getKeyword());
        assertEquals("CDE", emits.get(1).getKeyword());
    }
}
