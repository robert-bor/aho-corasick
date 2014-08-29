package org.ahocorasick.trie;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;
import java.lang.reflect.Field;

/**
 * <p>
 *     A state has various important tasks it must attend to:
 * </p>
 *
 * <ul>
 *     <li>success; when a character points to another state, it must return that state</li>
 *     <li>failure; when a character has no matching state, the algorithm must be able to fall back on a
 *         state with less depth</li>
 *     <li>emits; when this state is passed and keywords have been matched, the matches must be
 *         'emitted' so that they can be used later on.</li>
 * </ul>
 *
 * <p>
 *     The root state is special in the sense that it has no failure state; it cannot fail. If it 'fails'
 *     it will still parse the next character and start from the root node. This ensures that the algorithm
 *     always runs. All other states always have a fail state.
 * </p>
 *
 * @author Robert Bor
 */
public class State implements Serializable, Comparable<State> {

    /** effective the size of the keyword */
    private final int depth;

    /** only used for the root state to refer to itself in case no matches have been found */
    private final State rootState;

    /**
     * referred to in the white paper as the 'goto' structure. From a state it is possible to go
     * to other states, depending on the character passed.
     */
    private Map<Character,State> success = new TreeMap<Character, State>();

    /** if no matching states are found, the failure state will be returned */
    private State failure = null;

    /** whenever this state is reached, it will emit the matches keywords for future reference */
    private Set<String> emits = null;

    public State() {
        this(0);
    }

    public State(int depth) {
        this.depth = depth;
        this.rootState = depth == 0 ? this : null;
    }

    private State nextState(Character character, boolean ignoreRootState) {
        State nextState = this.success.get(character);
        if (!ignoreRootState && nextState == null && this.rootState != null) {
            nextState = this.rootState;
        }
        return nextState;
    }

    public State nextState(Character character) {
        return nextState(character, false);
    }

    public State nextStateIgnoreRootState(Character character) {
        return nextState(character, true);
    }

    public State addState(Character character) {
        State nextState = nextStateIgnoreRootState(character);
        if (nextState == null) {
            nextState = new State(this.depth+1);
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
        return this.emits == null ? Collections.<String> emptyList() : this.emits;
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

    public Collection<Character> getTransitions() {
        return this.success.keySet();
    }

    private void writeObject(java.io.ObjectOutputStream stream)
            throws IOException {
        stream.writeInt(depth);
        stream.writeObject(success);
        stream.writeObject(failure);
        stream.writeObject(emits);
    }

    private void readObject(java.io.ObjectInputStream stream)
            throws IOException, ClassNotFoundException, NoSuchFieldException, IllegalAccessException {

        // Use reflection to modify final field
        Field f = this.getClass().getDeclaredField("depth");
        f.setAccessible(true);
        f.set(this, stream.readInt());

        f = this.getClass().getDeclaredField("rootState");
        f.setAccessible(true);
        f.set(this, (depth == 0)?this:null);
        success = (TreeMap) stream.readObject();
        failure = (State) stream.readObject();
        emits = (List<String>) stream.readObject();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof State))
            return false;
        return compareTo((State) obj) == 0;
    }

    @Override
    public int compareTo(State o) {
        if (this.depth != o.depth)
            return 1;
        if ((this.depth == 0 && o.depth == 0) && (this.rootState != this || o.rootState != o))
            return 1;
        if (!this.success.equals(o.success))
            return 1;
        if (this.failure != null && o.failure != null && !this.failure.equals(o.failure))
            return 1;
        if (this.emits != null && o.emits != null && !this.emits.equals(o.emits))
            return 1;
        return 0;
    }
}
