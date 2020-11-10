package org.ahocorasick.trie;

import org.ahocorasick.trie.handler.AbstractStatefulPayloadEmitHandler;
import org.ahocorasick.trie.handler.PayloadEmitHandler;
import org.ahocorasick.trie.handler.StatefulPayloadEmitHandler;
import org.junit.Test;

import java.util.LinkedList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static java.util.Arrays.asList;
import static org.junit.Assert.*;

public class PayloadTrieTest {

    private final static String[] ALPHABET = new String[] { "abc", "bcd", "cde" };
    private final static String[] ALPHABET_PAYLOAD = new String[] { "alpha:abc", "alpha:bcd", "alpha:cde" };

    private final static List<Payload<String>> ALPHABET_WITH_PAYLOADS = asList(
            new Payload<>( ALPHABET[ 0 ], ALPHABET_PAYLOAD[ 0 ] ),
            new Payload<>( ALPHABET[ 1 ], ALPHABET_PAYLOAD[ 1 ] ),
            new Payload<>( ALPHABET[ 2 ], ALPHABET_PAYLOAD[ 2 ] ));

    private final static String[] PRONOUNS = new String[] { "hers", "his", "she", "he" };
    private final static int[] PRONOUNS_PAYLOAD_ID = new int[] { 9, 12, 4, 20 };

    private final static List<Payload<Integer>> PRONOUNS_WITH_PAYLOADS = asList(
        new Payload<>( PRONOUNS[ 0 ], PRONOUNS_PAYLOAD_ID[ 0 ] ),
        new Payload<>( PRONOUNS[ 1 ], PRONOUNS_PAYLOAD_ID[ 1 ] ),
        new Payload<>( PRONOUNS[ 2 ], PRONOUNS_PAYLOAD_ID[ 2 ] ),
        new Payload<>( PRONOUNS[ 3 ], PRONOUNS_PAYLOAD_ID[ 3 ] )
    );

    private final static String[] FOOD = new String[] { "veal", "cauliflower", "broccoli", "tomatoes" };
    private final static Food[] FOOD_PAYLOAD = new Food[] { new Food("veal"), new Food("cauliflower"), new Food("broccoli"),
            new Food("tomatoes") };

    private final static List<Payload<Food>> FOOD_WITH_PAYLOADS = asList(
        new Payload<>( FOOD[ 0 ], FOOD_PAYLOAD[ 0 ] ),
        new Payload<>( FOOD[ 1 ], FOOD_PAYLOAD[ 1 ] ),
        new Payload<>( FOOD[ 2 ], FOOD_PAYLOAD[ 2 ] ),
        new Payload<>( FOOD[ 3 ], FOOD_PAYLOAD[ 3 ] )
    );

    private final static String[] GREEK_LETTERS = new String[] { "Alpha", "Beta", "Gamma" };
    private final static String[] GREEK_LETTERS_PAYLOAD = new String[] { "greek:Alpha", "greek:Beta", "greek:Gamma" };

    private final static List<Payload<String>> GREEK_LETTERS_WITH_PAYLOADS = asList(
        new Payload<>( GREEK_LETTERS[ 0 ], GREEK_LETTERS_PAYLOAD[ 0 ] ),
        new Payload<>( GREEK_LETTERS[ 1 ], GREEK_LETTERS_PAYLOAD[ 1 ] ),
        new Payload<>( GREEK_LETTERS[ 2 ], GREEK_LETTERS_PAYLOAD[ 2 ] ));

    private final static String[] UNICODE = new String[] { "turning", "once", "again", "börkü" };
    private final static String[] UNICODE_PAYLOAD = new String[] { "uni:turning", "uni:once", "uni:again", "uni:börkü" };

    private final static List<Payload<String>> UNICODE_WITH_PAYLOADS = asList(
        new Payload<>( UNICODE[ 0 ], UNICODE_PAYLOAD[ 0 ] ),
        new Payload<>( UNICODE[ 1 ], UNICODE_PAYLOAD[ 1 ] ),
        new Payload<>( UNICODE[ 2 ], UNICODE_PAYLOAD[ 2 ] ),
        new Payload<>( UNICODE[ 3 ], UNICODE_PAYLOAD[ 3 ] ));

    public static class Food {
        private final String name;

        public Food(String name) {
            this.name = name;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((name == null) ? 0 : name.hashCode());
            return result;
        }

        @Override
        public boolean equals( Object obj ) {
            if( this == obj ) {
                return true;
            }
            if( obj == null ) {
                return false;
            }
            if( getClass() != obj.getClass() ) {
                return false;
            }
            Food other = (Food) obj;
            if( name == null ) {
                return other.name == null;
            }
            else {
                return name.equals( other.name );
            }
        }
    }

    @Test
    public void keywordAndTextAreTheSame() {
        PayloadTrie<String> trie = PayloadTrie.<String>builder().addKeyword(ALPHABET[0], ALPHABET_PAYLOAD[0]).build();
        Collection<PayloadEmit<String>> emits = trie.parseText(ALPHABET[0]);
        Iterator<PayloadEmit<String>> iterator = emits.iterator();
        checkEmit(iterator.next(), 0, 2, ALPHABET[0], ALPHABET_PAYLOAD[0]);
    }

    @Test
    public void keywordAndTextAreTheSameFirstMatch() {
        PayloadTrie<String> trie = PayloadTrie.<String>builder().addKeyword(ALPHABET[0], ALPHABET_PAYLOAD[0]).build();
        PayloadEmit<String> firstMatch = trie.firstMatch(ALPHABET[0]);
        checkEmit(firstMatch, 0, 2, ALPHABET[0], ALPHABET_PAYLOAD[0]);
    }

    @Test
    public void textIsLongerThanKeyword() {
        PayloadTrie<String> trie = PayloadTrie.<String>builder().addKeyword(ALPHABET[0], ALPHABET_PAYLOAD[0]).build();
        Collection<PayloadEmit<String>> emits = trie.parseText(" " + ALPHABET[0]);
        Iterator<PayloadEmit<String>> iterator = emits.iterator();
        checkEmit(iterator.next(), 1, 3, ALPHABET[0], ALPHABET_PAYLOAD[0]);
    }

    @Test
    public void textIsLongerThanKeywordFirstMatch() {

        PayloadTrie<String> trie = PayloadTrie.<String>builder().addKeyword(ALPHABET[0], ALPHABET_PAYLOAD[0]).build();
        PayloadEmit<String> firstMatch = trie.firstMatch(" " + ALPHABET[0]);
        checkEmit(firstMatch, 1, 3, ALPHABET[0], ALPHABET_PAYLOAD[0]);
    }

    @Test
    public void variousKeywordsOneMatch() {
        PayloadTrie<String> trie = PayloadTrie.<String>builder().addKeywords(ALPHABET_WITH_PAYLOADS).build();
        Collection<PayloadEmit<String>> emits = trie.parseText("bcd");
        Iterator<PayloadEmit<String>> iterator = emits.iterator();
        checkEmit(iterator.next(), 0, 2, "bcd", "alpha:bcd");
    }

    @Test
    public void variousKeywordsFirstMatch() {
        PayloadTrie<String> trie = PayloadTrie.<String>builder().addKeywords(ALPHABET_WITH_PAYLOADS).build();
        PayloadEmit<String> firstMatch = trie.firstMatch("bcd");
        checkEmit(firstMatch, 0, 2, "bcd", "alpha:bcd");
    }

    @Test
    public void ushersTestAndStopOnHit() {
        PayloadTrie<Integer> trie = PayloadTrie.<Integer>builder().addKeywords(PRONOUNS_WITH_PAYLOADS).stopOnHit().build();
        Collection<PayloadEmit<Integer>> emits = trie.parseText("ushers");
        assertEquals(1, emits.size()); // she @ 3, he @ 3, hers @ 5
        Iterator<PayloadEmit<Integer>> iterator = emits.iterator();
        checkEmit(iterator.next(), 2, 3, "he", 20);
    }

    @Test
    public void ushersTestStopOnHitSkipOne() {
        PayloadTrie<Integer> trie = PayloadTrie.<Integer>builder().addKeywords(PRONOUNS_WITH_PAYLOADS).stopOnHit().build();

        StatefulPayloadEmitHandler<Integer> testEmitHandler = new AbstractStatefulPayloadEmitHandler<Integer>() {
            boolean first = true;

            @Override
            public boolean emit(final PayloadEmit<Integer> emit) {
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
        Collection<PayloadEmit<Integer>> emits = testEmitHandler.getEmits();
        assertEquals(1, emits.size()); // she @ 3, he @ 3, hers @ 5
        Iterator<PayloadEmit<Integer>> iterator = emits.iterator();
        checkEmit(iterator.next(), 1, 3, "she", 4);
    }

    @Test
    public void ushersTest() {
        PayloadTrie<Integer> trie = PayloadTrie.<Integer>builder().addKeywords(PRONOUNS_WITH_PAYLOADS).build();
        Collection<PayloadEmit<Integer>> emits = trie.parseText("ushers");
        assertEquals(3, emits.size()); // she @ 3, he @ 3, hers @ 5
        Iterator<PayloadEmit<Integer>> iterator = emits.iterator();

        checkEmit(iterator.next(), 2, 3, "he", 20);
        checkEmit(iterator.next(), 1, 3, "she", 4);
        checkEmit(iterator.next(), 2, 5, "hers", 9);
    }

    @Test
    public void ushersTestWithCapitalKeywords() {
        PayloadTrie<String> trie = PayloadTrie.<String>builder().ignoreCase().addKeyword("HERS", "hers").addKeyword("HIS", "his")
                .addKeyword("SHE", "she").addKeyword("HE", "he").build();
        Collection<PayloadEmit<String>> emits = trie.parseText("ushers");
        assertEquals(3, emits.size()); // she @ 3, he @ 3, hers @ 5
        Iterator<PayloadEmit<String>> iterator = emits.iterator();
        checkEmit(iterator.next(), 2, 3, "HE", "he");
        checkEmit(iterator.next(), 1, 3, "SHE", "she");
        checkEmit(iterator.next(), 2, 5, "HERS", "hers");
    }

    @Test
    public void ushersTestFirstMatch() {
        PayloadTrie<Integer> trie = PayloadTrie.<Integer>builder().addKeywords(PRONOUNS_WITH_PAYLOADS).build();
        PayloadEmit<Integer> firstMatch = trie.firstMatch("ushers");
        checkEmit(firstMatch, 2, 3, "he", 20);
    }

    @Test
    public void ushersTestByCallback() {
        PayloadTrie<Integer> trie = PayloadTrie.<Integer>builder().addKeywords(PRONOUNS_WITH_PAYLOADS).build();

        final List<PayloadEmit<Integer>> emits = new LinkedList<>();
        PayloadEmitHandler<Integer> emitHandler = emit -> {
            emits.add(emit);
            return true;
        };
        trie.parseText("ushers", emitHandler);
        assertEquals(3, emits.size()); // she @ 3, he @ 3, hers @ 5
        Iterator<PayloadEmit<Integer>> iterator = emits.iterator();

        checkEmit(iterator.next(), 2, 3, "he", 20);
        checkEmit(iterator.next(), 1, 3, "she", 4);
        checkEmit(iterator.next(), 2, 5, "hers", 9);
    }

    @Test
    public void misleadingTest() {
        PayloadTrie<String> trie = PayloadTrie.<String>builder().addKeyword("hers", "pronon:hers").build();
        Collection<PayloadEmit<String>> emits = trie.parseText("h he her hers");
        Iterator<PayloadEmit<String>> iterator = emits.iterator();
        checkEmit(iterator.next(), 9, 12, "hers", "pronon:hers");
    }

    @Test
    public void misleadingTestFirstMatch() {
        PayloadTrie<String> trie = PayloadTrie.<String>builder().addKeyword("hers", "pronon:hers").build();
        PayloadEmit<String> firstMatch = trie.firstMatch("h he her hers");
        checkEmit(firstMatch, 9, 12, "hers", "pronon:hers");
    }

    @Test
    public void recipes() {
        PayloadTrie<Food> trie = PayloadTrie.<Food>builder().addKeywords(FOOD_WITH_PAYLOADS).build();
        Collection<PayloadEmit<Food>> emits = trie.parseText("2 cauliflowers, 3 tomatoes, 4 slices of veal, 100g broccoli");
        Iterator<PayloadEmit<Food>> iterator = emits.iterator();
        checkEmit(iterator.next(), 2, 12, "cauliflower", new Food("cauliflower"));
        checkEmit(iterator.next(), 18, 25, "tomatoes", new Food("tomatoes"));
        checkEmit(iterator.next(), 40, 43, "veal", new Food("veal"));
        checkEmit(iterator.next(), 51, 58, "broccoli", new Food("broccoli"));
    }

    @Test
    public void recipesFirstMatch() {
        PayloadTrie<Food> trie = PayloadTrie.<Food>builder().addKeywords(FOOD_WITH_PAYLOADS).build();
        PayloadEmit<Food> firstMatch = trie.firstMatch("2 cauliflowers, 3 tomatoes, 4 slices of veal, 100g broccoli");
        checkEmit(firstMatch, 2, 12, "cauliflower", new Food("cauliflower"));
    }

    @Test
    public void longAndShortOverlappingMatch() {
        PayloadTrie<String> trie = PayloadTrie.<String>builder().addKeyword("he", "pronon:he").addKeyword("hehehehe", "garbage")
                .build();
        Collection<PayloadEmit<String>> emits = trie.parseText("hehehehehe");
        Iterator<PayloadEmit<String>> iterator = emits.iterator();
        checkEmit(iterator.next(), 0, 1, "he", "pronon:he");
        checkEmit(iterator.next(), 2, 3, "he", "pronon:he");
        checkEmit(iterator.next(), 4, 5, "he", "pronon:he");
        checkEmit(iterator.next(), 6, 7, "he", "pronon:he");
        checkEmit(iterator.next(), 0, 7, "hehehehe", "garbage");
        checkEmit(iterator.next(), 8, 9, "he", "pronon:he");
        checkEmit(iterator.next(), 2, 9, "hehehehe", "garbage");
    }

    @Test
    public void nonOverlapping() {
        PayloadTrie<String> trie = PayloadTrie.<String>builder().removeOverlaps().addKeyword("ab", "alpha:ab")
                .addKeyword("cba", "alpha:cba").addKeyword("ababc", "alpha:ababc").build();
        Collection<PayloadEmit<String>> emits = trie.parseText("ababcbab");
        assertEquals(2, emits.size());
        Iterator<PayloadEmit<String>> iterator = emits.iterator();
        // With overlaps: ab@1, ab@3, ababc@4, cba@6, ab@7
        checkEmit(iterator.next(), 0, 4, "ababc", "alpha:ababc");
        checkEmit(iterator.next(), 6, 7, "ab", "alpha:ab");
    }

    @Test
    public void nonOverlappingFirstMatch() {
        PayloadTrie<String> trie = PayloadTrie.<String>builder().removeOverlaps().addKeyword("ab", "alpha:ab")
                .addKeyword("cba", "alpha:cba").addKeyword("ababc", "alpha:ababc").build();
        PayloadEmit<String> firstMatch = trie.firstMatch("ababcbab");

        checkEmit(firstMatch, 0, 4, "ababc", "alpha:ababc");
    }

    @Test
    public void containsMatch() {
        PayloadTrie<String> trie = PayloadTrie.<String>builder().removeOverlaps().addKeyword("ab", "alpha:ab")
                .addKeyword("cba", "alpha:cba").addKeyword("ababc", "alpha:ababc").build();
        assertTrue(trie.containsMatch("ababcbab"));
    }

    @Test
    public void startOfChurchillSpeech() {
        PayloadTrie<String> trie = PayloadTrie.<String>builder().removeOverlaps().addKeyword("T").addKeyword("u").addKeyword("ur")
                .addKeyword("r").addKeyword("urn").addKeyword("ni").addKeyword("i").addKeyword("in").addKeyword("n")
                .addKeyword("urning").build();
        Collection<PayloadEmit<String>> emits = trie.parseText("Turning");
        assertEquals(2, emits.size());
    }

    @Test
    public void partialMatch() {
        PayloadTrie<String> trie = PayloadTrie.<String>builder().onlyWholeWords().addKeyword("sugar", "food:sugar").build();
        Collection<PayloadEmit<String>> emits = trie.parseText("sugarcane sugarcane sugar canesugar"); // left, middle, right test
        assertEquals(1, emits.size()); // Match must not be made
        checkEmit(emits.iterator().next(), 20, 24, "sugar", "food:sugar");
    }

    @Test
    public void partialMatchFirstMatch() {
        PayloadTrie<String> trie = PayloadTrie.<String>builder().onlyWholeWords().addKeyword("sugar", "food:sugar").build();
        PayloadEmit<String> firstMatch = trie.firstMatch("sugarcane sugarcane sugar canesugar"); // left, middle, right test

        checkEmit(firstMatch, 20, 24, "sugar", "food:sugar");
    }

    @Test
    public void tokenizeFullSentence() {
        PayloadTrie<String> trie = PayloadTrie.<String>builder().addKeywords(GREEK_LETTERS_WITH_PAYLOADS).build();
        Collection<PayloadToken<String>> tokens = trie.tokenize("Hear: Alpha team first, Beta from the rear, Gamma in reserve");
        assertEquals(7, tokens.size());
        Iterator<PayloadToken<String>> tokensIt = tokens.iterator();
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
        PayloadTrie<String> trie = PayloadTrie.<String>builder().ignoreCase().onlyWholeWords().addKeywords(UNICODE_WITH_PAYLOADS)
                .build();
        Collection<PayloadEmit<String>> emits = trie.parseText("TurninG OnCe AgAiN BÖRKÜ");
        assertEquals(4, emits.size()); // Match must not be made
        Iterator<PayloadEmit<String>> it = emits.iterator();

        checkEmit(it.next(), 0, 6, "turning", "uni:turning");
        checkEmit(it.next(), 8, 11, "once", "uni:once");
        checkEmit(it.next(), 13, 17, "again", "uni:again");
        checkEmit(it.next(), 19, 23, "börkü", "uni:börkü");
    }

    @Test
    public void testIgnoreCase() {
        PayloadTrie<String> trie = PayloadTrie.<String>builder().ignoreCase().addKeywords(UNICODE_WITH_PAYLOADS).build();
        Collection<PayloadEmit<String>> emits = trie.parseText("TurninG OnCe AgAiN BÖRKÜ");
        assertEquals(4, emits.size()); // Match must not be made
        Iterator<PayloadEmit<String>> it = emits.iterator();

        checkEmit(it.next(), 0, 6, "turning", "uni:turning");
        checkEmit(it.next(), 8, 11, "once", "uni:once");
        checkEmit(it.next(), 13, 17, "again", "uni:again");
        checkEmit(it.next(), 19, 23, "börkü", "uni:börkü");
    }

    @Test
    public void testIgnoreCaseFirstMatch() {
        PayloadTrie<String> trie = PayloadTrie.<String>builder().ignoreCase().addKeywords(UNICODE_WITH_PAYLOADS).build();
        PayloadEmit<String> firstMatch = trie.firstMatch("TurninG OnCe AgAiN BÖRKÜ");

        checkEmit(firstMatch, 0, 6, "turning", "uni:turning");
    }

    @Test
    public void tokenizeTokensInSequence() {
        PayloadTrie<String> trie = PayloadTrie.<String>builder().addKeywords(GREEK_LETTERS_WITH_PAYLOADS).build();
        Collection<PayloadToken<String>> tokens = trie.tokenize("Alpha Beta Gamma");
        assertEquals(5, tokens.size());
    }

    // @see https://github.com/robert-bor/aho-corasick/issues/7
    @Test
    public void testZeroLength() {
        PayloadTrie<String> trie = PayloadTrie.<String>builder().ignoreOverlaps().onlyWholeWords().ignoreCase().addKeyword("")
                .build();
        trie.tokenize(
                "Try a natural lip and subtle bronzer to keep all the focus on those big bright eyes with NARS Eyeshadow Duo in Rated R And the winner is... Boots No7 Advanced Renewal Anti-ageing Glycolic Peel Kit ($25 amazon.com) won most-appealing peel.");
    }

    // @see https://github.com/robert-bor/aho-corasick/issues/8
    @Test
    public void testUnicode1() {
        String target = "LİKE THIS"; // The second character ('İ') is Unicode, which was read by AC as a 2-byte char
        assertEquals("THIS", target.substring(5, 9)); // Java does it the right way
        PayloadTrie<String> trie = PayloadTrie.<String>builder().ignoreCase().onlyWholeWords().addKeyword("this", "pronon:this")
                .build();
        Collection<PayloadEmit<String>> emits = trie.parseText(target);
        assertEquals(1, emits.size());
        Iterator<PayloadEmit<String>> it = emits.iterator();
        checkEmit(it.next(), 5, 8, "this", "pronon:this");
    }

    // @see https://github.com/robert-bor/aho-corasick/issues/8
    @Test
    public void testUnicode2() {
        String target = "LİKE THIS"; // The second character ('İ') is Unicode, which was read by AC as a 2-byte char
        PayloadTrie<String> trie = PayloadTrie.<String>builder().ignoreCase().onlyWholeWords().addKeyword("this", "pronon:this")
                .build();
        assertEquals("THIS", target.substring(5, 9)); // Java does it the right way
        PayloadEmit<String> firstMatch = trie.firstMatch(target);
        checkEmit(firstMatch, 5, 8, "this", "pronon:this");
    }

    @Test
    public void testPartialMatchWhiteSpaces() {
        PayloadTrie<String> trie = PayloadTrie.<String>builder().onlyWholeWordsWhiteSpaceSeparated()
                .addKeyword("#sugar-123", "sugar").build();
        Collection<PayloadEmit<String>> emits = trie.parseText("#sugar-123 #sugar-1234"); // left, middle, right test
        assertEquals(1, emits.size()); // Match must not be made
        checkEmit(emits.iterator().next(), 0, 9, "#sugar-123", "sugar");
    }

    @Test
    public void testLargeString() {
        final int interval = 100;
        final int textSize = 1000000;
        final String keyword = FOOD[1];
        final Food payload = FOOD_PAYLOAD[1];
        final StringBuilder text = randomNumbers(textSize);

        injectKeyword(text, keyword, interval);

        PayloadTrie<Food> trie = PayloadTrie.<Food>builder().onlyWholeWords().addKeyword(keyword, payload).build();

        final Collection<PayloadEmit<Food>> emits = trie.parseText(text);

        assertEquals(textSize / interval, emits.size());
    }

    @Test
    public void test_containsMatchWithCaseInsensitive() {
        PayloadTrie<String> trie = PayloadTrie.<String>builder().caseInsensitive().addKeyword("foo", "bar").build();

        assertTrue(trie.containsMatch("FOOBAR"));
        assertFalse(trie.containsMatch("FO!?AR"));
    }

    // @see https://github.com/robert-bor/aho-corasick/issues/85
    @Test
    public void test_wholeWords() {
        PayloadTrie<String> trie = PayloadTrie.<String>builder().addKeyword("foo", "bar").onlyWholeWords().build();
        // access via PayloadTrie.parseText(CharSequence)
        Collection<PayloadEmit<String>> result1 = trie.parseText("foobar");
        // access via PayloadTrie.parseText(CharSequence, PayloadEmitHandler<String>)
        Collection<PayloadEmit<String>> result2 = new LinkedList<>();
        trie.parseText("foobar", result2::add);

        assertTrue(result1.isEmpty());
        assertEquals(result1, result2);
    }

    // @see https://github.com/robert-bor/aho-corasick/issues/85
    @Test
    public void test_wholeWordsWhiteSpaceSeparated() {
        PayloadTrie<String> trie = PayloadTrie.<String>builder().addKeyword("foo", "bar").onlyWholeWordsWhiteSpaceSeparated().build();
        // access via PayloadTrie.parseText(CharSequence)
        Collection<PayloadEmit<String>> result1 = trie.parseText("foo#bar");
        // access via PayloadTrie.parseText(CharSequence, PayloadEmitHandler<String>)
        Collection<PayloadEmit<String>> result2 = new LinkedList<>();
        trie.parseText("foo#bar", result2::add);

        assertTrue(result1.isEmpty());
        assertEquals(result1, result2);
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

    private void checkEmit(PayloadEmit<Food> next, int expectedStart, int expectedEnd, String expectedKeyword,
            Food expectedPayload) {
        assertEquals("Start of emit should have been " + expectedStart, expectedStart, next.getStart());
        assertEquals("End of emit should have been " + expectedEnd, expectedEnd, next.getEnd());
        assertEquals("Keyword of emit shoud be " + expectedKeyword, expectedKeyword, next.getKeyword());
        assertEquals("Payload of emit shoud be " + expectedPayload, expectedPayload, next.getPayload());
    }

    private void checkEmit(PayloadEmit<Integer> next, int expectedStart, int expectedEnd, String expectedKeyword,
            Integer expectedPayload) {
        assertEquals("Start of emit should have been " + expectedStart, expectedStart, next.getStart());
        assertEquals("End of emit should have been " + expectedEnd, expectedEnd, next.getEnd());
        assertEquals("Keyword of emit shoud be " + expectedKeyword, expectedKeyword, next.getKeyword());
        assertEquals("Payload of emit shoud be " + expectedPayload, expectedPayload, next.getPayload());
    }

    private void checkEmit(PayloadEmit<String> next, int expectedStart, int expectedEnd, String expectedKeyword,
            String expectedPayload) {
        assertEquals("Start of emit should have been " + expectedStart, expectedStart, next.getStart());
        assertEquals("End of emit should have been " + expectedEnd, expectedEnd, next.getEnd());
        assertEquals("Keyword of emit shoud be " + expectedKeyword, expectedKeyword, next.getKeyword());
        assertEquals("Payload of emit shoud be " + expectedPayload, expectedPayload, next.getPayload());
    }
}
