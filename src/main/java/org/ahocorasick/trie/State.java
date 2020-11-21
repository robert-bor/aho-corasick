package org.ahocorasick.trie;

import it.unimi.dsi.fastutil.chars.Char2ObjectMap;
import it.unimi.dsi.fastutil.chars.Char2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.chars.CharSet;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

/**
 * <p>
 * A state has various important tasks it must attend to:
 * </p>
 * <ul>
 * <li>success; when a character points to another state, it must return that state</li>
 * <li>failure; when a character has no matching state, the algorithm must be able to fall back on a
 * state with less depth</li>
 * <li>emits; when this state is passed and keywords have been matched, the matches must be
 * 'emitted' so that they can be used later on.</li>
 * </ul>
 * <p>
 * The root state is special in the sense that it has no failure state; it cannot fail. If it 'fails'
 * it will still parse the next character and start from the root node. This ensures that the algorithm
 * always runs. All other states always have a fail state.
 * </p>
 *
 * @author Robert Bor
 */
public class State {

    /**
     * effective the size of the keyword
     */
    private final int depth;

    /**
     * only used for the root state to refer to itself in case no matches have been found
     */
    private final State rootState;

    /**
     * referred to in the white paper as the 'goto' structure. From a state it is possible to go
     * to other states, depending on the character passed.
     */
    private final Char2ObjectMap<State> success = new Char2ObjectOpenHashMap<>();

    /**
     * if no matching states are found, the failure state will be returned
     */
    private State failure;

    /**
     * whenever this state is reached, it will emit the matches keywords for future reference
     */
    private Set<String> emits;

    public State() {
        this(0);
    }

    public State(final int depth) {
        this.depth = depth;
        this.rootState = depth == 0 ? this : null;
    }

    private State nextState(final char character, final boolean ignoreRootState) {
        State nextState = this.success.get(character);

        if (!ignoreRootState && nextState == null && this.rootState != null) {
            nextState = this.rootState;
        }

        return nextState;
    }

    public State nextState(final char character) {
        return nextState(character, false);
    }

    public State nextStateIgnoreRootState(char character) {
        return nextState(character, true);
    }

    public State addState(String keyword) {
        State state = this;

        for (final char character : keyword.toCharArray()) {
            state = state.addState(character);
        }

        return state;
    }

    public State addState(char character) {
        State nextState = nextStateIgnoreRootState(character);
        if (nextState == null) {
            nextState = new State(this.depth + 1);
            this.success.put(character, nextState);
        }
        return nextState;
    }

    public int getDepth() {
        return this.depth;
    }

    public void addEmit(String keyword) {
        if (this.emits == null) {
            this.emits = new TreeSet<>();
        }
        this.emits.add(keyword);
    }

    public void addEmit(Collection<String> emits) {
        for (String emit : emits) {
            addEmit(emit);
        }
    }

    public Collection<String> emit() {
        return this.emits == null ? Collections.emptyList() : this.emits;
    }

    public State failure() {
        return this.failure;
    }

    public void setFailure(State failState) {
        this.failure = failState;
    }

    public Collection<State> getStates() {
        return this.success.values();
    }

    public CharSet getTransitions() {
        return this.success.keySet();
    }
}