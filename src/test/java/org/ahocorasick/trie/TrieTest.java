package org.ahocorasick.trie;

import org.junit.Test;

import java.util.Collection;
import java.util.Iterator;

import static junit.framework.Assert.assertEquals;

public class TrieTest {

    @Test
    public void keywordAndTextAreTheSame() {
        Trie trie = new Trie();
        trie.addKeyword("abc");
        Collection<Emit> emits = trie.parseText("abc");
        Iterator<Emit> iterator = emits.iterator();
        checkEmit(iterator.next(), 0, 2, "abc");
    }

    @Test
    public void textIsLongerThanKeyword() {
        Trie trie = new Trie();
        trie.addKeyword("abc");
        Collection<Emit> emits = trie.parseText(" abc");
        Iterator<Emit> iterator = emits.iterator();
        checkEmit(iterator.next(), 1, 3, "abc");
    }

    @Test
    public void variousKeywordsOneMatch() {
        Trie trie = new Trie();
        trie.addKeyword("abc");
        trie.addKeyword("bcd");
        trie.addKeyword("cde");
        Collection<Emit> emits = trie.parseText("bcd");
        Iterator<Emit> iterator = emits.iterator();
        checkEmit(iterator.next(), 0, 2, "bcd");
    }

    @Test
    public void ushersTest() {
        Trie trie = new Trie();
        trie.addKeyword("hers");
        trie.addKeyword("his");
        trie.addKeyword("she");
        trie.addKeyword("he");
        Collection<Emit> emits = trie.parseText("ushers");
        assertEquals(3, emits.size()); // she @ 3, he @ 3, hers @ 5
        Iterator<Emit> iterator = emits.iterator();
        checkEmit(iterator.next(), 1, 3, "she");
        checkEmit(iterator.next(), 2, 3, "he");
        checkEmit(iterator.next(), 2, 5, "hers");
    }

    @Test
    public void misleadingTest() {
        Trie trie = new Trie();
        trie.addKeyword("hers");
        Collection<Emit> emits = trie.parseText("h he her hers");
        Iterator<Emit> iterator = emits.iterator();
        checkEmit(iterator.next(), 9, 12, "hers");
    }

    @Test
    public void recipes() {
        Trie trie = new Trie();
        trie.addKeyword("veal");
        trie.addKeyword("cauliflower");
        trie.addKeyword("broccoli");
        trie.addKeyword("tomatoes");
        Collection<Emit> emits = trie.parseText("2 cauliflowers, 3 tomatoes, 4 slices of veal, 100g broccoli");
        Iterator<Emit> iterator = emits.iterator();
        checkEmit(iterator.next(), 2, 12, "cauliflower");
        checkEmit(iterator.next(), 18, 25, "tomatoes");
        checkEmit(iterator.next(), 40, 43, "veal");
        checkEmit(iterator.next(), 51, 58, "broccoli");
    }

    @Test
    public void longAndShortOverlappingMatch() {
        Trie trie = new Trie();
        trie.addKeyword("he");
        trie.addKeyword("hehehehe");
        Collection<Emit> emits = trie.parseText("hehehehehe");
        Iterator<Emit> iterator = emits.iterator();
        checkEmit(iterator.next(), 0, 1, "he");
        checkEmit(iterator.next(), 2, 3, "he");
        checkEmit(iterator.next(), 4, 5, "he");
        checkEmit(iterator.next(), 0, 7, "hehehehe");
        checkEmit(iterator.next(), 6, 7, "he");
        checkEmit(iterator.next(), 2, 9, "hehehehe");
        checkEmit(iterator.next(), 8, 9, "he");
    }

    @Test
    public void nonOverlapping() {
        Trie trie = new Trie().removeOverlaps();
        trie.addKeyword("ab");
        trie.addKeyword("cba");
        trie.addKeyword("ababc");
        Collection<Emit> emits = trie.parseText("ababcbab");
        assertEquals(2, emits.size());
        Iterator<Emit> iterator = emits.iterator();
        // With overlaps: ab@1, ab@3, ababc@4, cba@6, ab@7
        checkEmit(iterator.next(), 0, 4, "ababc");
        checkEmit(iterator.next(), 6, 7, "ab");
    }

    @Test
    public void startOfChurchillSpeech() {
        Trie trie = new Trie().removeOverlaps();
        trie.addKeyword("T");
        trie.addKeyword("u");
        trie.addKeyword("ur");
        trie.addKeyword("r");
        trie.addKeyword("urn");
        trie.addKeyword("ni");
        trie.addKeyword("i");
        trie.addKeyword("in");
        trie.addKeyword("n");
        trie.addKeyword("urning");
        Collection<Emit> emits = trie.parseText("Turning");
        assertEquals(2, emits.size());
    }

    @Test
    public void partialMatch() {
        Trie trie = new Trie().onlyWholeWords();
        trie.addKeyword("sugar");
        Collection<Emit> emits = trie.parseText("sugarcane sugarcane sugar canesugar"); // left, middle, right test
        assertEquals(1, emits.size()); // Match must not be made
        checkEmit(emits.iterator().next(), 20, 24, "sugar");
    }

    @Test
    public void tokenizeFullSentence() {
        Trie trie = new Trie();
        trie.addKeyword("Alpha");
        trie.addKeyword("Beta");
        trie.addKeyword("Gamma");
        Collection<Token> tokens = trie.tokenize("Hear: Alpha team first, Beta from the rear, Gamma in reserve");
        assertEquals(7, tokens.size());
        Iterator<Token> tokensIt = tokens.iterator();
        assertEquals("Hear: ", tokensIt.next().getFragment());
        assertEquals("Alpha", tokensIt.next().getFragment());
        assertEquals(" team first, ", tokensIt.next().getFragment());
        assertEquals("Beta", tokensIt.next().getFragment());
        assertEquals(" from the rear, ", tokensIt.next().getFragment());
        assertEquals("Gamma", tokensIt.next().getFragment());
        assertEquals(" in reserve", tokensIt.next().getFragment());
    }

    @Test
    public void bug5InGithubReportedByXCurry() {
        Trie trie = new Trie().caseInsensitive().onlyWholeWords();
        trie.addKeyword("turning");
        trie.addKeyword("once");
        trie.addKeyword("again");
        trie.addKeyword("börkü");
        Collection<Emit> emits = trie.parseText("TurninG OnCe AgAiN BÖRKÜ");
        assertEquals(4, emits.size()); // Match must not be made
        Iterator<Emit> it = emits.iterator();
        checkEmit(it.next(), 0, 6, "turning");
        checkEmit(it.next(), 8, 11, "once");
        checkEmit(it.next(), 13, 17, "again");
        checkEmit(it.next(), 19, 23, "börkü");
    }

    @Test
    public void caseInsensitive() {
        Trie trie = new Trie().caseInsensitive();
        trie.addKeyword("turning");
        trie.addKeyword("once");
        trie.addKeyword("again");
        trie.addKeyword("börkü");
        Collection<Emit> emits = trie.parseText("TurninG OnCe AgAiN BÖRKÜ");
        assertEquals(4, emits.size()); // Match must not be made
        Iterator<Emit> it = emits.iterator();
        checkEmit(it.next(), 0, 6, "turning");
        checkEmit(it.next(), 8, 11, "once");
        checkEmit(it.next(), 13, 17, "again");
        checkEmit(it.next(), 19, 23, "börkü");
    }

    @Test
    public void tokenizeTokensInSequence() {
        Trie trie = new Trie();
        trie.addKeyword("Alpha");
        trie.addKeyword("Beta");
        trie.addKeyword("Gamma");
        Collection<Token> tokens = trie.tokenize("Alpha Beta Gamma");
        assertEquals(5, tokens.size());
    }

    // Test offered by XCurry, https://github.com/robert-bor/aho-corasick/issues/7
    @Test
    public void zeroLengthTestBug7InGithubReportedByXCurry() {
        Trie trie = new Trie().removeOverlaps().onlyWholeWords().caseInsensitive();
        trie.addKeyword("");
        trie.tokenize("Try a natural lip and subtle bronzer to keep all the focus on those big bright eyes with NARS Eyeshadow Duo in Rated R And the winner is... Boots No7 Advanced Renewal Anti-ageing Glycolic Peel Kit ($25 amazon.com) won most-appealing peel.");
    }

    // Test offered by dwyerk, https://github.com/robert-bor/aho-corasick/issues/8
    @Test
    public void unicodeIssueBug8ReportedByDwyerk() {
        String target = "LİKE THIS"; // The second character ('İ') is Unicode, which was read by AC as a 2-byte char
        Trie trie = new Trie().caseInsensitive().onlyWholeWords();
        assertEquals("THIS", target.substring(5,9)); // Java does it the right way
        trie.addKeyword("this");
        Collection<Emit> emits = trie.parseText(target);
        assertEquals(1, emits.size());
        Iterator<Emit> it = emits.iterator();
        checkEmit(it.next(), 5, 8, "this");
    }

    private void checkEmit(Emit next, int expectedStart, int expectedEnd, String expectedKeyword) {
        assertEquals("Start of emit should have been "+expectedStart, expectedStart, next.getStart());
        assertEquals("End of emit should have been "+expectedEnd, expectedEnd, next.getEnd());
        assertEquals(expectedKeyword, next.getKeyword());
    }

}
