package org.ahocorasick.trie;

import org.junit.Test;

import java.util.Collection;
import java.util.Collections;

import static junit.framework.Assert.*;

public class StateTest {

    @Test
    public void constructSequenceOfCharacters() {
        final State rootState = new State();
        rootState
                .addState('a')
                .addState('b')
                .addState('c');
        State currentState = rootState.nextState('a');
        assertEquals(1, currentState.getDepth());
        currentState = currentState.nextState('b');
        assertEquals(2, currentState.getDepth());
        currentState = currentState.nextState('c');
        assertEquals(3, currentState.getDepth());
        currentState = currentState.nextState('F');
        assertNull(currentState);
    }

    @Test
    public void getStates() {
        State rootState = new State();
        rootState.addState("foo");

        State currentState = rootState.nextState('f');

        Collection<State> states = rootState.getStates();
        assertEquals(1, states.size());
        assertEquals(currentState, states.iterator().next());
    }


    @Test
    public void getTransitions() {
        State rootState = new State();
        rootState.addState("foo");
        State currentState = rootState.nextState('f');

        Collection<Character> transitions = rootState.getTransitions();
        assertEquals(1, transitions.size());
        assertEquals(Character.valueOf('f'), transitions.iterator().next());
    }

    @Test
    public void failure() {
        State failureState = new State();
        State rootState = new State();
        rootState.setFailure(failureState);

        assertEquals(failureState, rootState.failure());

    }

    @Test
    public void checkEmits() {
        State rootState = new State();
        rootState.addState('a')
                .addEmit(Collections.singleton("tag"));
        Collection<String> actual = rootState.nextState('a').emit();
        assertEquals(1, actual.size());
        assertEquals("tag", actual.iterator().next());
    }
}
