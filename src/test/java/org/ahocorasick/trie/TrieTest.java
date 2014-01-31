package org.ahocorasick.trie;

import org.ahocorasick.trie.Emit;
import org.ahocorasick.trie.Trie;
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
        Iterator<Emit> iterator = emits.iterator();
        // With overlaps: ab@1, ab@3, ababc@4, cba@6, ab@7
        checkEmit(iterator.next(), 0, 4, "ababc");
        checkEmit(iterator.next(), 6, 7, "ab");
    }

    private void checkEmit(Emit next, int expectedStart, int expectedEnd, String expectedKeyword) {
        assertEquals(expectedStart, next.getStart());
        assertEquals(expectedEnd, next.getEnd());
        assertEquals(expectedKeyword, next.getKeyword());
    }

}
