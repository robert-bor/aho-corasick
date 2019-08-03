package org.ahocorasick.trie;

import org.ahocorasick.trie.handler.AbstractStatefulEmitHandler;
import org.ahocorasick.trie.handler.EmitHandler;
import org.ahocorasick.trie.handler.StatefulEmitHandler;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TriePayloadTest {
    private final static String[] ALPHABET = new String[] { "abc", "bcd", "cde" };
    private final static String[] ALPHABET_PAYLOAD = new String[] { "payload:abc", "payload:bcd", "payload:cde" };
    private List<Payload<String>> ALPHABET_PAYLOADS = Arrays.asList(new Payload<String>(ALPHABET[0], ALPHABET_PAYLOAD[0]),
            new Payload<String>(ALPHABET[1], ALPHABET_PAYLOAD[1]), new Payload<String>(ALPHABET[2], ALPHABET_PAYLOAD[2]));

    private final static String[] PRONOUNS = new String[] { "hers", "his", "she", "he" };
    private final static String[] PRONOUNS_PAYLOAD = new String[] { "payload:hers", "payload:his", "payload:she", "payload:he" };
    private List<Payload<String>> PRONOUNS_PAYLOADS = Arrays.asList(new Payload<String>(PRONOUNS[0], PRONOUNS_PAYLOAD[0]),
            new Payload<String>(PRONOUNS[1], PRONOUNS_PAYLOAD[1]), new Payload<String>(PRONOUNS[2], PRONOUNS_PAYLOAD[2]),
            new Payload<String>(PRONOUNS[3], PRONOUNS_PAYLOAD[3]));

    private final static String[] FOOD = new String[] { "veal", "cauliflower", "broccoli", "tomatoes" };
    private final static String[] FOOD_PAYLOAD = new String[] { "payload:veal", "payload:cauliflower", "payload:broccoli",
            "payload:tomatoes" };
    private List<Payload<String>> FOOD_PAYLOADS = Arrays.asList(new Payload<String>(FOOD[0], FOOD_PAYLOAD[0]),
            new Payload<String>(FOOD[1], FOOD_PAYLOAD[1]), new Payload<String>(FOOD[2], FOOD_PAYLOAD[2]),
            new Payload<String>(FOOD[3], FOOD_PAYLOAD[3]));

    private final static String[] GREEK_LETTERS = new String[] { "Alpha", "Beta", "Gamma" };
    private final static String[] GREEK_PAYLOAD = new String[] { "payload:alpha", "payload:beta", "payload:gamma" };
    private List<Payload<String>> GREEK_PAYLOADS = Arrays.asList(new Payload<String>(GREEK_LETTERS[0], GREEK_PAYLOAD[0]),
            new Payload<String>(GREEK_LETTERS[1], GREEK_PAYLOAD[1]), new Payload<String>(GREEK_LETTERS[2], GREEK_PAYLOAD[2]));

    private final static String[] UNICODE = new String[] { "turning", "once", "again", "börkü" };
    private final static String[] UNICODE_PAYLOAD = new String[] { "payload:turning", "payload:once", "payload:again",
            "payload:börkü" };
    private List<Payload<String>> UNICODE_PAYLOADS = Arrays.asList(new Payload<String>(UNICODE[0], UNICODE_PAYLOAD[0]),
            new Payload<String>(UNICODE[1], UNICODE_PAYLOAD[1]), new Payload<String>(UNICODE[2], UNICODE_PAYLOAD[2]),
            new Payload<String>(UNICODE[3], UNICODE_PAYLOAD[3]));

    @Test
    public void keywordAndTextAreTheSame() {
        Trie<String> trie = Trie.<String>payloadBuilder().addKeyword(ALPHABET[0], ALPHABET_PAYLOAD[0]).build();
        Collection<Emit<String>> emits = trie.parseText(ALPHABET[0]);
        Iterator<Emit<String>> iterator = emits.iterator();
        checkEmit(iterator.next(), 0, 2, ALPHABET_PAYLOAD[0]);
    }

    @Test
    public void keywordAndTextAreTheSameFirstMatch() {
        Trie<String> trie = Trie.<String>payloadBuilder().addKeyword(ALPHABET[0], ALPHABET_PAYLOAD[0]).build();
        Emit<String> firstMatch = trie.firstMatch(ALPHABET[0]);
        checkEmit(firstMatch, 0, 2, ALPHABET_PAYLOAD[0]);
    }

    @Test
    public void textIsLongerThanKeyword() {
        Trie<String> trie = Trie.<String>payloadBuilder().addKeyword(ALPHABET[0], ALPHABET_PAYLOAD[0]).build();
        Collection<Emit<String>> emits = trie.parseText(" " + ALPHABET[0]);
        Iterator<Emit<String>> iterator = emits.iterator();
        checkEmit(iterator.next(), 1, 3, ALPHABET_PAYLOAD[0]);
    }

    @Test
    public void textIsLongerThanKeywordFirstMatch() {
        Trie<String> trie = Trie.<String>payloadBuilder().addKeyword(ALPHABET[0], ALPHABET_PAYLOAD[0]).build();
        Emit<String> firstMatch = trie.firstMatch(" " + ALPHABET[0]);
        checkEmit(firstMatch, 1, 3, ALPHABET_PAYLOAD[0]);
    }

    @Test
    public void variousKeywordsOneMatch() {
        Trie<String> trie = Trie.<String>payloadBuilder().addKeywords(ALPHABET_PAYLOADS).build();
        Collection<Emit<String>> emits = trie.parseText("bcd");
        Iterator<Emit<String>> iterator = emits.iterator();
        checkEmit(iterator.next(), 0, 2, "payload:bcd");
    }

    @Test
    public void variousKeywordsFirstMatch() {
        Trie<String> trie = Trie.<String>payloadBuilder().addKeywords(ALPHABET_PAYLOADS).build();
        Emit<String> firstMatch = trie.firstMatch("bcd");
        checkEmit(firstMatch, 0, 2, "payload:bcd");
    }

    @Test
    public void ushersTestAndStopOnHit() {
        Trie<String> trie = Trie.<String>payloadBuilder().addKeywords(PRONOUNS_PAYLOADS).stopOnHit().build();
        Collection<Emit<String>> emits = trie.parseText("ushers");
        assertEquals(1, emits.size()); // she @ 3, he @ 3, hers @ 5
        Iterator<Emit<String>> iterator = emits.iterator();
        checkEmit(iterator.next(), 2, 3, "payload:he");
    }

    @Test
    public void ushersTestStopOnHitSkipOne() {
        Trie<String> trie = Trie.<String>payloadBuilder().addKeywords(PRONOUNS_PAYLOADS).stopOnHit().build();

        StatefulEmitHandler<String> testEmitHandler = new AbstractStatefulEmitHandler<String>() {
            boolean first = true;

            @Override
            public boolean emit(final Emit<String> emit) {
                if (first) {
                    // return false for the first element
                    first = false;
                    return false;
                }
                addEmit(emit);
                return true;
            }

        };

        trie.parseText("ushers", testEmitHandler);
        Collection<Emit<String>> emits = testEmitHandler.getEmits();
        assertEquals(1, emits.size()); // she @ 3, he @ 3, hers @ 5
        Iterator<Emit<String>> iterator = emits.iterator();
        checkEmit(iterator.next(), 1, 3, "payload:she");
    }

    @Test
    public void ushersTest() {
        Trie<String> trie = Trie.<String>payloadBuilder().addKeywords(PRONOUNS_PAYLOADS).build();
        Collection<Emit<String>> emits = trie.parseText("ushers");
        assertEquals(3, emits.size()); // she @ 3, he @ 3, hers @ 5
        Iterator<Emit<String>> iterator = emits.iterator();
        checkEmit(iterator.next(), 2, 3, "payload:he");
        checkEmit(iterator.next(), 1, 3, "payload:she");
        checkEmit(iterator.next(), 2, 5, "payload:hers");
    }

    @Test
    public void ushersTestWithCapitalKeywords() {
        Trie<String> trie = Trie.<String>payloadBuilder().ignoreCase().addKeyword("HERS", "payload:HERS")
                .addKeyword("HIS", "payload:HIS").addKeyword("SHE", "payload:SHE").addKeyword("HE", "payload:HE").build();
        Collection<Emit<String>> emits = trie.parseText("ushers");
        assertEquals(3, emits.size()); // she @ 3, he @ 3, hers @ 5
        Iterator<Emit<String>> iterator = emits.iterator();
        checkEmit(iterator.next(), 2, 3, "payload:HE");
        checkEmit(iterator.next(), 1, 3, "payload:SHE");
        checkEmit(iterator.next(), 2, 5, "payload:HERS");
    }

    @Test
    public void ushersTestFirstMatch() {
        Trie<String> trie = Trie.<String>payloadBuilder().addKeywords(PRONOUNS_PAYLOADS).build();
        Emit<String> firstMatch = trie.firstMatch("ushers");
        checkEmit(firstMatch, 2, 3, "payload:he");
    }

    @Test
    public void ushersTestByCallback() {
        Trie<String> trie = Trie.<String>payloadBuilder().addKeywords(PRONOUNS_PAYLOADS).build();

        final List<Emit<String>> emits = new ArrayList<>();
        EmitHandler<String> emitHandler = new EmitHandler<String>() {

            @Override
            public boolean emit(Emit<String> emit) {
                emits.add(emit);
                return true;
            }
        };
        trie.parseText("ushers", emitHandler);
        assertEquals(3, emits.size()); // she @ 3, he @ 3, hers @ 5
        Iterator<Emit<String>> iterator = emits.iterator();
        checkEmit(iterator.next(), 2, 3, "payload:he");
        checkEmit(iterator.next(), 1, 3, "payload:she");
        checkEmit(iterator.next(), 2, 5, "payload:hers");
    }

    @Test
    public void misleadingTest() {
        Trie<String> trie = Trie.<String>payloadBuilder().addKeyword("hers", "payload:hers").build();
        Collection<Emit<String>> emits = trie.parseText("h he her hers");
        Iterator<Emit<String>> iterator = emits.iterator();
        checkEmit(iterator.next(), 9, 12, "payload:hers");
    }

    @Test
    public void misleadingTestFirstMatch() {
        Trie<String> trie = Trie.<String>payloadBuilder().addKeyword("hers", "payload:hers").build();
        Emit<String> firstMatch = trie.firstMatch("h he her hers");
        checkEmit(firstMatch, 9, 12, "payload:hers");
    }

    @Test
    public void recipes() {
        Trie<String> trie = Trie.<String>payloadBuilder().addKeywords(FOOD_PAYLOADS).build();
        Collection<Emit<String>> emits = trie.parseText("2 cauliflowers, 3 tomatoes, 4 slices of veal, 100g broccoli");
        Iterator<Emit<String>> iterator = emits.iterator();
        checkEmit(iterator.next(), 2, 12, "payload:cauliflower");
        checkEmit(iterator.next(), 18, 25, "payload:tomatoes");
        checkEmit(iterator.next(), 40, 43, "payload:veal");
        checkEmit(iterator.next(), 51, 58, "payload:broccoli");
    }

    @Test
    public void recipesFirstMatch() {
        Trie<String> trie = Trie.<String>payloadBuilder().addKeywords(FOOD_PAYLOADS).build();
        Emit firstMatch = trie.firstMatch("2 cauliflowers, 3 tomatoes, 4 slices of veal, 100g broccoli");

        checkEmit(firstMatch, 2, 12, "payload:cauliflower");
    }

    @Test
    public void longAndShortOverlappingMatch() {
        Trie<String> trie = Trie.<String>payloadBuilder().addKeyword("he", "payload:he")
                .addKeyword("hehehehe", "payload:hehehehe").build();
        Collection<Emit<String>> emits = trie.parseText("hehehehehe");
        Iterator<Emit<String>> iterator = emits.iterator();
        checkEmit(iterator.next(), 0, 1, "payload:he");
        checkEmit(iterator.next(), 2, 3, "payload:he");
        checkEmit(iterator.next(), 4, 5, "payload:he");
        checkEmit(iterator.next(), 6, 7, "payload:he");
        checkEmit(iterator.next(), 0, 7, "payload:hehehehe");
        checkEmit(iterator.next(), 8, 9, "payload:he");
        checkEmit(iterator.next(), 2, 9, "payload:hehehehe");
    }

    @Test
    public void nonOverlapping() {
        Trie<String> trie = Trie.<String>payloadBuilder().removeOverlaps().addKeyword("ab", "payload:ab")
                .addKeyword("cba", "payload:cba").addKeyword("ababc", "payload:ababc").build();
        Collection<Emit<String>> emits = trie.parseText("ababcbab");
        assertEquals(2, emits.size());
        Iterator<Emit<String>> iterator = emits.iterator();
        // With overlaps: ab@1, ab@3, ababc@4, cba@6, ab@7
        checkEmit(iterator.next(), 0, 4, "payload:ababc");
        checkEmit(iterator.next(), 6, 7, "payload:ab");
    }

    @Test
    public void nonOverlappingFirstMatch() {
        Trie<String> trie = Trie.<String>payloadBuilder().removeOverlaps().addKeyword("ab", "payload:ab")
                .addKeyword("cba", "payload:cba").addKeyword("ababc", "payload:ababc").build();
        Emit<String> firstMatch = trie.firstMatch("ababcbab");

        checkEmit(firstMatch, 0, 4, "payload:ababc");
    }

    @Test
    public void containsMatch() {
        Trie<String> trie = Trie.<String>payloadBuilder().removeOverlaps().addKeyword("ab", "payload:ab")
                .addKeyword("cba", "payload:cba").addKeyword("ababc", "payload:ababc").build();
        assertTrue(trie.containsMatch("payload:ababcbab"));
    }

    @Test
    public void startOfChurchillSpeech() {
        Trie<String> trie = Trie.<String>payloadBuilder().removeOverlaps().addKeyword("T", "payload:T")
                .addKeyword("u", "payload:u").addKeyword("ur", "payload:ur").addKeyword("r", "payload:r")
                .addKeyword("urn", "payload:urn").addKeyword("ni", "payload:ni").addKeyword("i", "payload:i")
                .addKeyword("in", "payload:in").addKeyword("n", "payload:n").addKeyword("urning", "payload:urning").build();
        Collection<Emit<String>> emits = trie.parseText("Turning");
        assertEquals(2, emits.size());
    }

    @Test
    public void partialMatch() {
        Trie<String> trie = Trie.<String>payloadBuilder().onlyWholeWords().addKeyword("sugar", "payload:sugar").build();
        Collection<Emit<String>> emits = trie.parseText("sugarcane sugarcane sugar canesugar"); // left, middle, right test
        assertEquals(1, emits.size()); // Match must not be made
        checkEmit(emits.iterator().next(), 20, 24, "payload:sugar");
    }

    @Test
    public void partialMatchFirstMatch() {
        Trie<String> trie = Trie.<String>payloadBuilder().onlyWholeWords().addKeyword("sugar", "payload:sugar").build();
        Emit<String> firstMatch = trie.firstMatch("sugarcane sugarcane sugar canesugar"); // left, middle, right test

        checkEmit(firstMatch, 20, 24, "payload:sugar");
    }

    @Test
    public void tokenizeFullSentence() {
        Trie<String> trie = Trie.<String>payloadBuilder().addKeywords(GREEK_PAYLOADS).build();
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
        Trie<String> trie = Trie.<String>payloadBuilder().ignoreCase().onlyWholeWords().addKeywords(UNICODE_PAYLOADS).build();
        Collection<Emit<String>> emits = trie.parseText("TurninG OnCe AgAiN BÖRKÜ");
        assertEquals(4, emits.size()); // Match must not be made
        Iterator<Emit<String>> it = emits.iterator();
        checkEmit(it.next(), 0, 6, "payload:turning");
        checkEmit(it.next(), 8, 11, "payload:once");
        checkEmit(it.next(), 13, 17, "payload:again");
        checkEmit(it.next(), 19, 23, "payload:börkü");
    }

    @Test
    public void testIgnoreCase() {
        Trie<String> trie = Trie.<String>payloadBuilder().ignoreCase().addKeywords(UNICODE_PAYLOADS).build();
        Collection<Emit<String>> emits = trie.parseText("TurninG OnCe AgAiN BÖRKÜ");
        assertEquals(4, emits.size()); // Match must not be made
        Iterator<Emit<String>> it = emits.iterator();
        checkEmit(it.next(), 0, 6, "payload:turning");
        checkEmit(it.next(), 8, 11, "payload:once");
        checkEmit(it.next(), 13, 17, "payload:again");
        checkEmit(it.next(), 19, 23, "payload:börkü");
    }

    @Test
    public void testIgnoreCaseFirstMatch() {
        Trie<String> trie = Trie.<String>payloadBuilder().ignoreCase().addKeywords(UNICODE_PAYLOADS).build();
        Emit<String> firstMatch = trie.firstMatch("TurninG OnCe AgAiN BÖRKÜ");

        checkEmit(firstMatch, 0, 6, "payload:turning");
    }

    @Test
    public void tokenizeTokensInSequence() {
        Trie<String> trie = Trie.<String>payloadBuilder().addKeywords(GREEK_PAYLOADS).build();
        Collection<Token> tokens = trie.tokenize("Alpha Beta Gamma");
        assertEquals(5, tokens.size());
    }

    // @see https://github.com/robert-bor/aho-corasick/issues/7
    @Test
    public void testZeroLength() {
        Trie<String> trie = Trie.<String>payloadBuilder().ignoreOverlaps().onlyWholeWords().ignoreCase().addKeyword("", "")
                .build();
        trie.tokenize(
                "Try a natural lip and subtle bronzer to keep all the focus on those big bright eyes with NARS Eyeshadow Duo in Rated R And the winner is... Boots No7 Advanced Renewal Anti-ageing Glycolic Peel Kit ($25 amazon.com) won most-appealing peel.");
    }

    // @see https://github.com/robert-bor/aho-corasick/issues/8
    @Test
    public void testUnicode1() {
        String target = "LİKE THIS"; // The second character ('İ') is Unicode, which was read by AC as a 2-byte char
        assertEquals("THIS", target.substring(5, 9)); // Java does it the right way
        Trie<String> trie = Trie.<String>payloadBuilder().ignoreCase().onlyWholeWords().addKeyword("this", "payload:this")
                .build();
        Collection<Emit<String>> emits = trie.parseText(target);
        assertEquals(1, emits.size());
        Iterator<Emit<String>> it = emits.iterator();
        checkEmit(it.next(), 5, 8, "payload:this");
    }

    // @see https://github.com/robert-bor/aho-corasick/issues/8
    @Test
    public void testUnicode2() {
        String target = "LİKE THIS"; // The second character ('İ') is Unicode, which was read by AC as a 2-byte char
        Trie<String> trie = Trie.<String>payloadBuilder().ignoreCase().onlyWholeWords().addKeyword("this", "payload:this")
                .build();
        assertEquals("THIS", target.substring(5, 9)); // Java does it the right way
        Emit<String> firstMatch = trie.firstMatch(target);
        checkEmit(firstMatch, 5, 8, "payload:this");
    }

    @Test
    public void testPartialMatchWhiteSpaces() {
        Trie<String> trie = Trie.<String>payloadBuilder().onlyWholeWordsWhiteSpaceSeparated()
                .addKeyword("#sugar-123", "payload:#sugar-123").build();
        Collection<Emit<String>> emits = trie.parseText("#sugar-123 #sugar-1234"); // left, middle, right test
        assertEquals(1, emits.size()); // Match must not be made
        checkEmit(emits.iterator().next(), 0, 9, "payload:#sugar-123");
    }

    @Test
    public void testLargeString() {
        final int interval = 100;
        final int textSize = 1000000;
        final String keyword = FOOD[1];
        final StringBuilder text = randomNumbers(textSize);

        injectKeyword(text, keyword, interval);

        Trie<String> trie = Trie.<String>payloadBuilder().onlyWholeWords().addKeyword(keyword, "payload:" + keyword).build();

        final Collection<Emit<String>> emits = trie.parseText(text);

        assertEquals(textSize / interval, emits.size());
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
     * @param source   Should contain a bunch of random data that cannot match any
     *                 keyword.
     * @param keyword  A keyword to inject repeatedly in the text.
     * @param interval How often to inject the keyword.
     */
    private void injectKeyword(final StringBuilder source, final String keyword, final int interval) {
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
        assertEquals(expectedKeyword, next.getPayload().getData());
    }
}
