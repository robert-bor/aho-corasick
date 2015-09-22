package org.ahocorasick.trie;

import org.ahocorasick.trie.handler.EmitHandler;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TrieTest {

    @Test
    public void keywordAndTextAreTheSame() {
        Trie trie = Trie.builder()
            .addKeyword("abc")
            .build();
        Collection<Emit> emits = trie.parseText("abc");
        Iterator<Emit> iterator = emits.iterator();
        checkEmit(iterator.next(), 0, 2, "abc");
    }

	 @Test
    public void keywordAndTextAreTheSameFirstMatch() {
        Trie trie = Trie.builder()
                .addKeyword("abc")
                .build();
		Emit firstMatch = trie.firstMatch("abc");
        checkEmit(firstMatch, 0, 2, "abc");
    }

    @Test
    public void textIsLongerThanKeyword() {
        Trie trie = Trie.builder()
            .addKeyword("abc")
            .build();
        Collection<Emit> emits = trie.parseText(" abc");
        Iterator<Emit> iterator = emits.iterator();
        checkEmit(iterator.next(), 1, 3, "abc");
    }

	@Test
    public void textIsLongerThanKeywordFirstMatch() {
        Trie trie = Trie.builder()
                .addKeyword("abc")
                .build();
		Emit firstMatch = trie.firstMatch(" abc");
        checkEmit(firstMatch, 1, 3, "abc");
    }

    @Test
    public void variousKeywordsOneMatch() {
        Trie trie = Trie.builder()
            .addKeyword("abc")
            .addKeyword("bcd")
            .addKeyword("cde")
            .build();
        Collection<Emit> emits = trie.parseText("bcd");
        Iterator<Emit> iterator = emits.iterator();
        checkEmit(iterator.next(), 0, 2, "bcd");
    }

	@Test
    public void variousKeywordsFirstMatch() {
        Trie trie = Trie.builder()
                .addKeyword("abc")
                .addKeyword("bcd")
                .addKeyword("cde")
                .build();
		Emit firstMatch = trie.firstMatch("bcd");
        checkEmit(firstMatch, 0, 2, "bcd");
    }

    @Test
    public void ushersTestAndStopOnHit() {
        Trie trie = Trie.builder()
                .addKeyword("hers")
                .addKeyword("his")
                .addKeyword("she")
                .addKeyword("he")
                .stopOnHit()
                .build();
        Collection<Emit> emits = trie.parseText("ushers");
        assertEquals(2, emits.size()); // she @ 3, he @ 3, hers @ 5
        Iterator<Emit> iterator = emits.iterator();
        checkEmit(iterator.next(), 2, 3, "he");
        checkEmit(iterator.next(), 1, 3, "she");
    }

    @Test
    public void ushersTest() {
        Trie trie = Trie.builder()
                .addKeyword("hers")
                .addKeyword("his")
                .addKeyword("she")
                .addKeyword("he")
                .build();
        Collection<Emit> emits = trie.parseText("ushers");
        assertEquals(3, emits.size()); // she @ 3, he @ 3, hers @ 5
        Iterator<Emit> iterator = emits.iterator();
        checkEmit(iterator.next(), 2, 3, "he");
        checkEmit(iterator.next(), 1, 3, "she");
        checkEmit(iterator.next(), 2, 5, "hers");
    }

    @Test
    public void ushersTestWithCapitalKeywords() {
        Trie trie = Trie.builder()
                .caseInsensitive()
                .addKeyword("HERS")
                .addKeyword("HIS")
                .addKeyword("SHE")
                .addKeyword("HE")
                .build();
        Collection<Emit> emits = trie.parseText("ushers");
        assertEquals(3, emits.size()); // she @ 3, he @ 3, hers @ 5
        Iterator<Emit> iterator = emits.iterator();
        checkEmit(iterator.next(), 2, 3, "he");
        checkEmit(iterator.next(), 1, 3, "she");
        checkEmit(iterator.next(), 2, 5, "hers");
    }

    @Test
    public void ushersTestFirstMatch() {
         Trie trie = Trie.builder()
                .addKeyword("hers")
                .addKeyword("his")
                .addKeyword("she")
                .addKeyword("he")
                .build();
		Emit firstMatch = trie.firstMatch("ushers");
		checkEmit(firstMatch, 2, 3, "he");
	}

    @Test
    public void ushersTestByCallback() {
        Trie trie = Trie.builder()
                .addKeyword("hers")
                .addKeyword("his")
                .addKeyword("she")
                .addKeyword("he")
                .build();

        final List<Emit> emits = new ArrayList<>();
        EmitHandler emitHandler = new EmitHandler() {

            @Override
            public void emit(Emit emit) {
                emits.add(emit);
            }
        };
        trie.parseText("ushers", emitHandler);
        assertEquals(3, emits.size()); // she @ 3, he @ 3, hers @ 5
        Iterator<Emit> iterator = emits.iterator();
        checkEmit(iterator.next(), 2, 3, "he");
        checkEmit(iterator.next(), 1, 3, "she");
        checkEmit(iterator.next(), 2, 5, "hers");
    }

    @Test
    public void misleadingTest() {
        Trie trie = Trie.builder()
            .addKeyword("hers")
            .build();
        Collection<Emit> emits = trie.parseText("h he her hers");
        Iterator<Emit> iterator = emits.iterator();
        checkEmit(iterator.next(), 9, 12, "hers");
    }

	 @Test
    public void misleadingTestFirstMatch() {
         Trie trie = Trie.builder()
                 .addKeyword("hers")
                 .build();
		Emit firstMatch = trie.firstMatch("h he her hers");
        checkEmit(firstMatch, 9, 12, "hers");
    }

    @Test
    public void recipes() {
        Trie trie = Trie.builder()
            .addKeyword("veal")
            .addKeyword("cauliflower")
            .addKeyword("broccoli")
            .addKeyword("tomatoes")
            .build();
        Collection<Emit> emits = trie.parseText("2 cauliflowers, 3 tomatoes, 4 slices of veal, 100g broccoli");
        Iterator<Emit> iterator = emits.iterator();
        checkEmit(iterator.next(), 2, 12, "cauliflower");
        checkEmit(iterator.next(), 18, 25, "tomatoes");
        checkEmit(iterator.next(), 40, 43, "veal");
        checkEmit(iterator.next(), 51, 58, "broccoli");
    }

	 @Test
    public void recipesFirstMatch() {
         Trie trie = Trie.builder()
                 .addKeyword("veal")
                 .addKeyword("cauliflower")
                 .addKeyword("broccoli")
                 .addKeyword("tomatoes")
                 .build();
		Emit firstMatch = trie.firstMatch("2 cauliflowers, 3 tomatoes, 4 slices of veal, 100g broccoli");

        checkEmit(firstMatch, 2, 12, "cauliflower");
    }

    @Test
    public void longAndShortOverlappingMatch() {
        Trie trie = Trie.builder()
                .addKeyword("he")
                .addKeyword("hehehehe")
                .build();
        Collection<Emit> emits = trie.parseText("hehehehehe");
        Iterator<Emit> iterator = emits.iterator();
        checkEmit(iterator.next(), 0, 1, "he");
        checkEmit(iterator.next(), 2, 3, "he");
        checkEmit(iterator.next(), 4, 5, "he");
        checkEmit(iterator.next(), 6, 7, "he");
        checkEmit(iterator.next(), 0, 7, "hehehehe");
        checkEmit(iterator.next(), 8, 9, "he");
        checkEmit(iterator.next(), 2, 9, "hehehehe");
    }

    @Test
    public void nonOverlapping() {
        Trie trie = Trie.builder().removeOverlaps()
            .addKeyword("ab")
            .addKeyword("cba")
            .addKeyword("ababc")
            .build();
        Collection<Emit> emits = trie.parseText("ababcbab");
        assertEquals(2, emits.size());
        Iterator<Emit> iterator = emits.iterator();
        // With overlaps: ab@1, ab@3, ababc@4, cba@6, ab@7
        checkEmit(iterator.next(), 0, 4, "ababc");
        checkEmit(iterator.next(), 6, 7, "ab");
    }

    @Test
    public void nonOverlappingFirstMatch() {
         Trie trie = Trie.builder().removeOverlaps()
                 .addKeyword("ab")
                 .addKeyword("cba")
                 .addKeyword("ababc")
                 .build();
		Emit firstMatch = trie.firstMatch("ababcbab");

        checkEmit(firstMatch, 0, 4, "ababc");
    }

    @Test
    public void containsMatch() {
         Trie trie = Trie.builder().removeOverlaps()
                 .addKeyword("ab")
                 .addKeyword("cba")
                 .addKeyword("ababc")
                 .build();
        assertTrue(trie.containsMatch("ababcbab"));
    }

    @Test
    public void startOfChurchillSpeech() {
        Trie trie = Trie.builder().removeOverlaps()
            .addKeyword("T")
            .addKeyword("u")
            .addKeyword("ur")
            .addKeyword("r")
            .addKeyword("urn")
            .addKeyword("ni")
            .addKeyword("i")
            .addKeyword("in")
            .addKeyword("n")
            .addKeyword("urning")
            .build();
        Collection<Emit> emits = trie.parseText("Turning");
        assertEquals(2, emits.size());
    }

    @Test
    public void partialMatch() {
        Trie trie = Trie.builder()
            .onlyWholeWords()
            .addKeyword("sugar")
            .build();
        Collection<Emit> emits = trie.parseText("sugarcane sugarcane sugar canesugar"); // left, middle, right test
        assertEquals(1, emits.size()); // Match must not be made
        checkEmit(emits.iterator().next(), 20, 24, "sugar");
    }

	   @Test
    public void partialMatchFirstMatch() {
        Trie trie = Trie.builder()
                .onlyWholeWords()
                .addKeyword("sugar")
                .build();
		Emit firstMatch = trie.firstMatch("sugarcane sugarcane sugar canesugar"); // left, middle, right test
        
        checkEmit(firstMatch, 20, 24, "sugar");
    }

    @Test
    public void tokenizeFullSentence() {
        Trie trie = Trie.builder()
            .addKeyword("Alpha")
            .addKeyword("Beta")
            .addKeyword("Gamma")
            .build();
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
        Trie trie = Trie.builder().caseInsensitive().onlyWholeWords()
            .addKeyword("turning")
            .addKeyword("once")
            .addKeyword("again")
            .addKeyword("börkü")
            .build();
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
        Trie trie = Trie.builder().caseInsensitive()
            .addKeyword("turning")
            .addKeyword("once")
            .addKeyword("again")
            .addKeyword("börkü")
            .build();
        Collection<Emit> emits = trie.parseText("TurninG OnCe AgAiN BÖRKÜ");
        assertEquals(4, emits.size()); // Match must not be made
        Iterator<Emit> it = emits.iterator();
        checkEmit(it.next(), 0, 6, "turning");
        checkEmit(it.next(), 8, 11, "once");
        checkEmit(it.next(), 13, 17, "again");
        checkEmit(it.next(), 19, 23, "börkü");
    }

	@Test
    public void caseInsensitiveFirstMatch() {
        Trie trie = Trie.builder().caseInsensitive()
                .addKeyword("turning")
                .addKeyword("once")
                .addKeyword("again")
                .addKeyword("börkü")
                .build();
		Emit firstMatch = trie.firstMatch("TurninG OnCe AgAiN BÖRKÜ");

        checkEmit(firstMatch, 0, 6, "turning");
    }

    @Test
    public void tokenizeTokensInSequence() {
        Trie trie = Trie.builder()
            .addKeyword("Alpha")
            .addKeyword("Beta")
            .addKeyword("Gamma")
            .build();
        Collection<Token> tokens = trie.tokenize("Alpha Beta Gamma");
        assertEquals(5, tokens.size());
    }

    // Test offered by XCurry, https://github.com/robert-bor/aho-corasick/issues/7
    @Test
    public void zeroLengthTestBug7InGithubReportedByXCurry() {
        Trie trie = Trie.builder().removeOverlaps().onlyWholeWords().caseInsensitive()
            .addKeyword("")
            .build();
        trie.tokenize("Try a natural lip and subtle bronzer to keep all the focus on those big bright eyes with NARS Eyeshadow Duo in Rated R And the winner is... Boots No7 Advanced Renewal Anti-ageing Glycolic Peel Kit ($25 amazon.com) won most-appealing peel.");
    }

    // Test offered by dwyerk, https://github.com/robert-bor/aho-corasick/issues/8
    @Test
    public void unicodeIssueBug8ReportedByDwyerk() {
        String target = "LİKE THIS"; // The second character ('İ') is Unicode, which was read by AC as a 2-byte char
        assertEquals("THIS", target.substring(5, 9)); // Java does it the right way
        Trie trie = Trie.builder().caseInsensitive().onlyWholeWords()
            .addKeyword("this")
            .build();
        Collection<Emit> emits = trie.parseText(target);
        assertEquals(1, emits.size());
        Iterator<Emit> it = emits.iterator();
        checkEmit(it.next(), 5, 8, "this");
    }

	 @Test
    public void unicodeIssueBug8ReportedByDwyerkFirstMatch() {
        String target = "LİKE THIS"; // The second character ('İ') is Unicode, which was read by AC as a 2-byte char
        Trie trie = Trie.builder()
                .caseInsensitive()
                .onlyWholeWords()
                .addKeyword("this")
                .build();
        assertEquals("THIS", target.substring(5, 9)); // Java does it the right way
		Emit firstMatch = trie.firstMatch(target);
        checkEmit(firstMatch, 5, 8, "this");
    }

    private void checkEmit(Emit next, int expectedStart, int expectedEnd, String expectedKeyword) {
        assertEquals("Start of emit should have been "+expectedStart, expectedStart, next.getStart());
        assertEquals("End of emit should have been "+expectedEnd, expectedEnd, next.getEnd());
        assertEquals(expectedKeyword, next.getKeyword());
    }

}
