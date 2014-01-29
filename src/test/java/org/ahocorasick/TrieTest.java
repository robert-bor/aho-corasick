package org.ahocorasick;

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
        checkEmit(iterator.next(), 2, "abc");
    }

    @Test
    public void textIsLongerThanKeyword() {
        Trie trie = new Trie();
        trie.addKeyword("abc");
        Collection<Emit> emits = trie.parseText(" abc");
        Iterator<Emit> iterator = emits.iterator();
        checkEmit(iterator.next(), 3, "abc");
    }

    @Test
    public void variousKeywordsOneMatch() {
        Trie trie = new Trie();
        trie.addKeyword("abc");
        trie.addKeyword("bcd");
        trie.addKeyword("cde");
        Collection<Emit> emits = trie.parseText("bcd");
        Iterator<Emit> iterator = emits.iterator();
        checkEmit(iterator.next(), 2, "bcd");
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
        checkEmit(iterator.next(), 3, "she");
        checkEmit(iterator.next(), 3, "he");
        checkEmit(iterator.next(), 5, "hers");
    }

    @Test
    public void misleadingTest() {
        Trie trie = new Trie();
        trie.addKeyword("hers");
        Collection<Emit> emits = trie.parseText("h he her hers");
        Iterator<Emit> iterator = emits.iterator();
        checkEmit(iterator.next(), 12, "hers");
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
        checkEmit(iterator.next(), 12, "cauliflower");
        checkEmit(iterator.next(), 25, "tomatoes");
        checkEmit(iterator.next(), 43, "veal");
        checkEmit(iterator.next(), 58, "broccoli");
    }

    private void checkEmit(Emit next, int expectedPosition, String expectedKeyword) {
        assertEquals(expectedPosition, next.getPosition());
        assertEquals(expectedKeyword, next.getKeyword());
    }

}
