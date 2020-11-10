package org.ahocorasick.trie;

import org.junit.Test;

import java.util.Collection;
import java.util.Collections;

import static org.junit.Assert.*;

public class StateTest {

    @Test
    public void test_constructSequenceOfCharacters() {
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
    public void test_getStates() {
        final State rootState = new State();
        rootState.addState("foo");
        final State currentState = rootState.nextState('f');
        final Collection<State> states = rootState.getStates();

        assertEquals(1, states.size());
        assertEquals(currentState, states.iterator().next());
    }

    @Test
    public void test_getTransitions() {
        final State rootState = new State();
        rootState.addState("foo");
        final State currentState = rootState.nextState('f');
        final Collection<Character> transitions = rootState.getTransitions();

        assertEquals(1, transitions.size());
        assertEquals(Character.valueOf('f'), transitions.iterator().next());
    }

    @Test
    public void test_failure() {
        final State failureState = new State();
        final State rootState = new State();
        rootState.setFailure(failureState);

        assertEquals(failureState, rootState.failure());
    }

    @Test
    public void test_checkEmits() {
        final State rootState = new State();
        rootState.addState('a')
                .addEmit(Collections.singleton("tag"));
        final Collection<String> actual = rootState.nextState('a').emit();

        assertEquals(1, actual.size());
        assertEquals("tag", actual.iterator().next());
    }

}
