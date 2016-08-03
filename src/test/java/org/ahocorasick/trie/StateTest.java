package org.ahocorasick.trie;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;

public class StateTest {

    @Test
    public void constructSequenceOfCharacters() {
        State rootState = new State();
        Transition a = new CharacterTransition('a');
        Transition b = new CharacterTransition('b');
        Transition c = new CharacterTransition('c');
        rootState
            .addState(a)
            .addState(b)
            .addState(c);
        State currentState = rootState.nextState(a);
        assertEquals(1, currentState.getDepth());
        currentState = currentState.nextState(b);
        assertEquals(2, currentState.getDepth());
        currentState = currentState.nextState(c);
        assertEquals(3, currentState.getDepth());
    }
    
    @Test
    public void constructSequenceOfWords() {
        State rootState = new State();
        Transition a = new WordTransition("Alpha");
        Transition b = new WordTransition("Bravo");
        Transition c = new WordTransition("Charlie");
        rootState
            .addState(a)
            .addState(b)
            .addState(c);
        State currentState = rootState.nextState(a);
        assertEquals(1, currentState.getDepth());
        currentState = currentState.nextState(b);
        assertEquals(2, currentState.getDepth());
        currentState = currentState.nextState(c);
        assertEquals(3, currentState.getDepth());
    }

}
