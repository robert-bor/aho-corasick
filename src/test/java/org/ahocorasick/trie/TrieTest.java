package org.ahocorasick.trie;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

import org.ahocorasick.trie.handler.AbstractStatefulEmitHandler;
import org.ahocorasick.trie.handler.EmitHandler;
import org.ahocorasick.trie.handler.StatefulEmitHandler;
import org.junit.Test;

public class TrieTest {
    private final static String[] ALPHABET = new String[]{
            "abc", "bcd", "cde"
    };

    private final static String[] PRONOUNS = new String[]{
            "hers", "his", "she", "he"
    };

    private final static String[] FOOD = new String[]{
            "veal", "cauliflower", "broccoli", "tomatoes"
    };

    private final static String[] GREEK_LETTERS = new String[]{
            "Alpha", "Beta", "Gamma"
    };

    private final static String[] UNICODE = new String[]{
            "turning", "once", "again", "börkü"
    };

    @Test
    public void keywordAndTextAreTheSame() {
        Trie trie = Trie.builder()
                .addKeyword(ALPHABET[0])
                .build();
        Collection<Emit> emits = trie.parseText(ALPHABET[0]);
        Iterator<Emit> iterator = emits.iterator();
        checkEmit(iterator.next(), 0, 2, ALPHABET[0]);
    }

    @Test
    public void keywordAndTextAreTheSameFirstMatch() {
        Trie trie = Trie.builder()
                .addKeyword(ALPHABET[0])
                .build();
        Emit firstMatch = trie.firstMatch(ALPHABET[0]);
        checkEmit(firstMatch, 0, 2, ALPHABET[0]);
    }

    @Test
    public void textIsLongerThanKeyword() {
        Trie trie = Trie.builder()
                .addKeyword(ALPHABET[0])
                .build();
        Collection<Emit> emits = trie.parseText(" " + ALPHABET[0]);
        Iterator<Emit> iterator = emits.iterator();
        checkEmit(iterator.next(), 1, 3, ALPHABET[0]);
    }

    @Test
    public void textIsLongerThanKeywordFirstMatch() {
        Trie trie = Trie.builder()
                .addKeyword(ALPHABET[0])
                .build();
        Emit firstMatch = trie.firstMatch(" " + ALPHABET[0]);
        checkEmit(firstMatch, 1, 3, ALPHABET[0]);
    }

    @Test
    public void variousKeywordsOneMatch() {
        Trie trie = Trie.builder()
                .addKeywords(ALPHABET)
                .build();
        Collection<Emit> emits = trie.parseText("bcd");
        Iterator<Emit> iterator = emits.iterator();
        checkEmit(iterator.next(), 0, 2, "bcd");
    }

    @Test
    public void variousKeywordsFirstMatch() {
        Trie trie = Trie.builder()
                .addKeywords(ALPHABET)
                .build();
        Emit firstMatch = trie.firstMatch("bcd");
        checkEmit(firstMatch, 0, 2, "bcd");
    }

    @Test
    public void ushersTestAndStopOnHit() {
        Trie trie = Trie.builder()
                .addKeywords(PRONOUNS)
                .stopOnHit()
                .build();
        Collection<Emit> emits = trie.parseText("ushers");
        assertEquals(1, emits.size()); // she @ 3, he @ 3, hers @ 5
        Iterator<Emit> iterator = emits.iterator();
        checkEmit(iterator.next(), 2, 3, "he");
    }

    @Test
    public void ushersTestStopOnHitSkipOne() {
        Trie trie = Trie.builder()
                .addKeywords(PRONOUNS)
                .stopOnHit()
                .build();
        
        StatefulEmitHandler testEmitHandler = new AbstractStatefulEmitHandler() {
            boolean first = true;

            @Override
            public boolean emit(final Emit emit) {
                if(first) {
                    // return false for the first element
                    first = false;
                    return false;
                }
                addEmit(emit);
                return true;
            }

        };
        
        trie.parseText("ushers", testEmitHandler);
        Collection<Emit> emits = testEmitHandler.getEmits();
        assertEquals(1, emits.size()); // she @ 3, he @ 3, hers @ 5
        Iterator<Emit> iterator = emits.iterator();
        checkEmit(iterator.next(), 1, 3, "she");
    }

    @Test
    public void ushersTest() {
        Trie trie = Trie.builder()
                .addKeywords(PRONOUNS)
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
                .ignoreCase()
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
                .addKeywords(PRONOUNS)
                .build();
        Emit firstMatch = trie.firstMatch("ushers");
        checkEmit(firstMatch, 2, 3, "he");
    }

    @Test
    public void ushersTestByCallback() {
        Trie trie = Trie.builder()
                .addKeywords(PRONOUNS)
                .build();

        final List<Emit> emits = new ArrayList<>();
        EmitHandler emitHandler = new EmitHandler() {

            @Override
            public boolean emit(Emit emit) {
                emits.add(emit);
                return true;
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
                .addKeywords(FOOD)
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
                .addKeywords(FOOD)
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
                .addKeywords(GREEK_LETTERS)
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

    // @see https://github.com/robert-bor/aho-corasick/issues/5
    @Test
    public void testStringIndexOutOfBoundsException() {
        Trie trie = Trie.builder().ignoreCase().onlyWholeWords()
                .addKeywords(UNICODE)
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
    public void testIgnoreCase() {
        Trie trie = Trie.builder().ignoreCase()
                .addKeywords(UNICODE)
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
    public void testIgnoreCaseFirstMatch() {
        Trie trie = Trie.builder().ignoreCase()
                .addKeywords(UNICODE)
                .build();
        Emit firstMatch = trie.firstMatch("TurninG OnCe AgAiN BÖRKÜ");

        checkEmit(firstMatch, 0, 6, "turning");
    }

    @Test
    public void tokenizeTokensInSequence() {
        Trie trie = Trie.builder()
                .addKeywords(GREEK_LETTERS)
                .build();
        Collection<Token> tokens = trie.tokenize("Alpha Beta Gamma");
        assertEquals(5, tokens.size());
    }

    // @see https://github.com/robert-bor/aho-corasick/issues/7
    @Test
    public void testZeroLength() {
        Trie trie = Trie.builder().ignoreOverlaps().onlyWholeWords().ignoreCase()
                .addKeyword("")
                .build();
        trie.tokenize("Try a natural lip and subtle bronzer to keep all the focus on those big bright eyes with NARS Eyeshadow Duo in Rated R And the winner is... Boots No7 Advanced Renewal Anti-ageing Glycolic Peel Kit ($25 amazon.com) won most-appealing peel.");
    }

    // @see https://github.com/robert-bor/aho-corasick/issues/8
    @Test
    public void testUnicode1() {
        String target = "LİKE THIS"; // The second character ('İ') is Unicode, which was read by AC as a 2-byte char
        assertEquals("THIS", target.substring(5, 9)); // Java does it the right way
        Trie trie = Trie.builder().ignoreCase().onlyWholeWords()
                .addKeyword("this")
                .build();
        Collection<Emit> emits = trie.parseText(target);
        assertEquals(1, emits.size());
        Iterator<Emit> it = emits.iterator();
        checkEmit(it.next(), 5, 8, "this");
    }

    // @see https://github.com/robert-bor/aho-corasick/issues/8
    @Test
    public void testUnicode2() {
        String target = "LİKE THIS"; // The second character ('İ') is Unicode, which was read by AC as a 2-byte char
        Trie trie = Trie.builder()
                .ignoreCase()
                .onlyWholeWords()
                .addKeyword("this")
                .build();
        assertEquals("THIS", target.substring(5, 9)); // Java does it the right way
        Emit firstMatch = trie.firstMatch(target);
        checkEmit(firstMatch, 5, 8, "this");
    }

    @Test
    public void testPartialMatchWhiteSpaces() {
        Trie trie = Trie.builder()
                .onlyWholeWordsWhiteSpaceSeparated()
                .addKeyword("#sugar-123")
                .build();
        Collection<Emit> emits = trie.parseText("#sugar-123 #sugar-1234"); // left, middle, right test
        assertEquals(1, emits.size()); // Match must not be made
        checkEmit(emits.iterator().next(), 0, 9, "#sugar-123");
    }

    @Test
    public void testLargeString() {
        final int interval = 100;
        final int textSize = 1000000;
        final String keyword = FOOD[1];
        final StringBuilder text = randomNumbers(textSize);

        injectKeyword(text, keyword, interval);

        Trie trie = Trie.builder()
                .onlyWholeWords()
                .addKeyword(keyword)
                .build();

        final Collection<Emit> emits = trie.parseText(text);

        assertEquals(textSize / interval, emits.size());
    }

    @Test(timeout=30_000)
    public void testParallelSearch() throws InterruptedException {
        final int interval = 100;
        final int textSize = 1000000;
        final String keyword = FOOD[1];
        final StringBuilder matchingText = randomNumbers(textSize);
        injectKeyword(matchingText, keyword, interval);
        final StringBuilder nonMatchingText = randomNumbers(textSize);
        injectKeyword(nonMatchingText, keyword.substring(0, keyword.length()-1), interval);

        final Trie trie = Trie.builder()
                .onlyWholeWords()
                .addKeyword(keyword)
                .build();

        final AtomicInteger matchCount = new AtomicInteger(0);
        Runnable matchingTask = new Runnable() {
            @Override
            public void run() {
                matchCount.set(trie.parseText(matchingText).size());
            }
        };

        final AtomicInteger nonMatchCount = new AtomicInteger(0);
        Runnable nonMatchingTask = new Runnable() {
            @Override
            public void run() {
                nonMatchCount.set(trie.parseText(nonMatchingText).size());
            }
        };
        Thread matchingThread = new Thread(matchingTask);
        Thread nonMatchingThread = new Thread(nonMatchingTask);
        matchingThread.start();
        nonMatchingThread.start();
        matchingThread.join();
        nonMatchingThread.join();

        assertEquals(textSize / interval, matchCount.get());
        assertEquals(0, nonMatchCount.get());
    }

    /**
     * Generates a random sequence of ASCII numbers.
     *
     * @param count The number of numbers to generate.
     * @return A character sequence filled with random digits.
     */
    private StringBuilder randomNumbers(int count) {
        final StringBuilder sb = new StringBuilder(count);

        while (--count > 0) {
            sb.append(randomInt(0, 10));
        }

        return sb;
    }

    /**
     * Injects keywords into a string builder.
     *
     * @param source   Should contain a bunch of random data that cannot match
     *                 any keyword.
     * @param keyword  A keyword to inject repeatedly in the text.
     * @param interval How often to inject the keyword.
     */
    private void injectKeyword(
            final StringBuilder source,
            final String keyword,
            final int interval) {
        final int length = source.length();
        for (int i = 0; i < length; i += interval) {
            source.replace(i, i + keyword.length(), keyword);
        }
    }

    private int randomInt(final int min, final int max) {
        return ThreadLocalRandom.current().nextInt(min, max);
    }

    private void checkEmit(Emit next, int expectedStart, int expectedEnd, String expectedKeyword) {
        assertEquals("Start of emit should have been " + expectedStart, expectedStart, next.getStart());
        assertEquals("End of emit should have been " + expectedEnd, expectedEnd, next.getEnd());
        assertEquals(expectedKeyword, next.getKeyword());
    }
}
