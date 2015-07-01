package org.ahocorasick.trie;

import org.apache.commons.lang3.SerializationUtils;
import org.junit.Test;

import java.io.Serializable;

import static junit.framework.Assert.assertEquals;

public class StateTest {

    @Test
    public void constructSequenceOfCharacters() {
        State rootState = new State();
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
    }

    @Test
    public void testSerialization() {
        State rootState = new State();
        rootState
                .addState('a')
                .addState('b')
                .addState('c');
        State currentState = rootState.nextState('a');
        currentState = currentState.nextState('b');
        currentState = currentState.nextState('c');

        Serializable copy = SerializationUtils.clone(rootState);
        assertEquals(copy, rootState);
    }

}
