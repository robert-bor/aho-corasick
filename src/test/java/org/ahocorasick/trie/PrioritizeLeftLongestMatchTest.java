package org.ahocorasick.trie;

import org.junit.Test;

import java.util.Collection;
import java.util.Iterator;

import static org.junit.Assert.assertEquals;

public class PrioritizeLeftLongestMatchTest {

    /**
     * This test shows correct matching of the longest, left-most matches
     */
    @Test
    public void testPrioritizeLeftLongestMatchExpectedBehaviour() {
        final Trie trie = Trie.builder()
                .ignoreCase()
                .ignoreOverlaps()
                .onlyWholeWords()
                .addKeyword("RESPIRATORY TRACT INFECTIONS")  // correct: match
                .addKeyword("URINARY TRACT INFECTIONS")  // correct: match
                .addKeyword("URINARY")  // correct: no match
                .addKeyword("INTRA-ABDOMINAL INFECTIONS")  // correct: match
                .build();

        final Collection<Emit> emits = trie.parseText(
                "lower respiratory tract infections, urinary tract infections, intra-abdominal infections, and more.");

        assertEquals(3, emits.size()); // she @ 3, he @ 3, hers @ 5
        final Iterator<Emit> iterator = emits.iterator();

        // expected as longest match on start index 6
        checkEmit(iterator.next(), 6, 33, "RESPIRATORY TRACT INFECTIONS");

        // expected as longest match on start index 36
        checkEmit(iterator.next(), 36, 59, "URINARY TRACT INFECTIONS");

        // expected, as longest match on start index 62
        checkEmit(iterator.next(), 62, 87, "INTRA-ABDOMINAL INFECTIONS");

    }

    /**
     * This test shows that when exchanging the keyword "intra-abdominal infections" for "infections, intra-abdominal",
     * the 2nd match from left to right in the text is wrong, as "urinary" is matched over "urinary tract infections"
     */
    @Test
    public void testPrioritizeLeftLongestMatchUnexpectedBehaviour() {
        final Trie trie = Trie.builder()
                .ignoreCase()
                .ignoreOverlaps()
                .onlyWholeWords()
                .addKeyword("RESPIRATORY TRACT INFECTIONS")  // correct: match
                .addKeyword("URINARY TRACT INFECTIONS")  // incorrect: no match
                .addKeyword("URINARY")  // incorrect: match
                .addKeyword("INFECTIONS, INTRA-ABDOMINAL")  // incorrect: match
                .build();

        final Collection<Emit> emits = trie.parseText(
                "lower respiratory tract infections, urinary tract infections, intra-abdominal infections, and more.");

        assertEquals(3, emits.size()); // she @ 3, he @ 3, hers @ 5
        final Iterator<Emit> iterator = emits.iterator();

        // expected as longest match on start index 6
        checkEmit(iterator.next(), 6, 33, "RESPIRATORY TRACT INFECTIONS");

        // unexpected, as "urinary tract infections" shares same start index 6 and "urinary" is a shorter match
        checkEmit(iterator.next(), 36, 42, "URINARY");

        // unexpected, as "urinary tract infections" should be the left-longest match from previous iteration and
        // "infections" starting at index 50 should not be considered anymore
        checkEmit(iterator.next(), 50, 76, "INFECTIONS, INTRA-ABDOMINAL");
    }

  private void checkEmit( Emit next, int expectedStart, int expectedEnd,
                          String expectedKeyword ) {
    assertEquals("Start of emit should have been " + expectedStart, expectedStart, next.getStart());
    assertEquals("End of emit should have been " + expectedEnd, expectedEnd, next.getEnd());
    assertEquals(expectedKeyword, next.getKeyword());
  }
}
