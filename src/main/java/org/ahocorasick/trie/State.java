package org.ahocorasick.trie;

import com.google.common.base.Objects;

import java.io.*;
import java.lang.reflect.Field;
import java.util.*;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * <p>
 * A state has various important tasks it must attend to:
 * </p>
 * <p>
 * <ul>
 * <li>success; when a character points to another state, it must return that state</li>
 * <li>failure; when a character has no matching state, the algorithm must be able to fall back on a
 * state with less depth</li>
 * <li>emits; when this state is passed and keywords have been matched, the matches must be
 * 'emitted' so that they can be used later on.</li>
 * </ul>
 * <p>
 * <p>
 * The root state is special in the sense that it has no failure state; it cannot fail. If it 'fails'
 * it will still parse the next character and start from the root node. This ensures that the algorithm
 * always runs. All other states always have a fail state.
 * </p>
 *
 * @author Robert Bor
 */
public class State implements Serializable {

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
    private Map<Character, State> success = new TreeMap<>();

    /**
     * if no matching states are found, the failure state will be returned
     */
    private State failure = null;

    /**
     * whenever this state is reached, it will emit the matches keywords for future reference
     */
    private Set<String> emits = null;

    public State() {
        this(0);
    }

    private State(int depth) {
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

    private State nextStateIgnoreRootState(Character character) {
        return nextState(character, true);
    }

    public State addState(Character character) {
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
        emits.forEach(this::addEmit);
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

    public Collection<Character> getTransitions() {
        return this.success.keySet();
    }

    private void writeObject(StateObjectOutputStream stream)
            throws IOException {
        stream.writeInt(this.depth);
        stream.writeInt(this.success.size());
        for (Map.Entry<Character, State> e : this.success.entrySet()) {
            stream.writeObject(e.getKey());

            Integer reference = stream.objectToReference.get(e.getValue());
            if (reference == null) {
                stream.objectToReference.put(e.getValue(), stream.incrementAndGetReferenceCount());
                stream.writeInt(0);
                stream.writeInt(stream.getReferenceCount());
                stream.writeObject(e.getValue());
            } else {
                stream.writeInt(reference);
            }
        }
        stream.writeObject(this.emits);
    }

    private void writeObject(ObjectOutputStream stream)
            throws IOException {
        if (stream instanceof StateObjectOutputStream) {
            writeObject((StateObjectOutputStream) stream);
        } else {
            // this is the root state
            IdentityHashMap<Object, Integer> objectToReference = new IdentityHashMap<>();
            writeObject(new StateObjectOutputStream(stream, objectToReference, 1));
        }
    }

    private void readObject(StateObjectInputStream stream)
            throws IOException, ClassNotFoundException, NoSuchFieldException, IllegalAccessException {

        // Use reflection to modify final field
        Field f = this.getClass().getDeclaredField("depth");
        f.setAccessible(true);
        f.set(this, checkNotNull(stream.readInt()));

        f = this.getClass().getDeclaredField("rootState");
        f.setAccessible(true);
        f.set(this, (depth == 0) ? this : null);
        int successSize = checkNotNull(stream.readInt());
        success = new TreeMap<>();
        for (int i = 0; i < successSize; i++) {
            Character character = checkNotNull((Character) stream.readObject());
            Integer reference = checkNotNull(stream.readInt());
            State treeState;
            if (reference == 0) {
                Integer referenceID = checkNotNull(stream.readInt());
                treeState = checkNotNull((State) stream.readObject());
                stream.getReferenceToObject().put(referenceID, treeState);
            } else {
                try {
                    treeState = checkNotNull((State) stream.getReferenceToObject().get(reference));
                } catch (NullPointerException e) {
                    throw new RuntimeException("reference=" + reference + ", " + stream.getReferenceToObject().size(), e);
                }
            }
            success.put(character, treeState);
        }
        emits = (TreeSet) stream.readObject();
    }

    private void readObject(ObjectInputStream stream)
            throws IOException, ClassNotFoundException, NoSuchFieldException, IllegalAccessException {
        if (stream instanceof StateObjectInputStream) {
            readObject((StateObjectInputStream) stream);
        } else {
            // this is the root state
            IdentityHashMap<Integer, Object> referenceToObject = new IdentityHashMap<>();
            readObject(new StateObjectInputStream(stream, referenceToObject));
            // failure was not serialized/deserialized as it complicates logic, let's just reconstruct it
            Trie.constructFailureStates(this);
        }
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null || getClass() != other.getClass()) return false;
        State state = (State) other;
        return equals(this, state, new IdentityHashMap<>());
    }

    private boolean equals(State self, State other, IdentityHashMap<State, Integer> equalityReferenceMap) {
        if (self == other) return true;
        return !(self == null || other == null) &&
                self.depth == other.depth &&
                ((self.depth > 0 && self.rootState == null && other.rootState == null) ||
                 (self.depth == 0 && self == self.rootState && other == other.rootState)) &&
                gotoEquality(self.success, other.success, equalityReferenceMap) &&
                equals(self.failure, other.failure, equalityReferenceMap) && Objects.equal(self.emits, other.emits);
    }

    private boolean gotoEquality(Map<Character, State> mine, Map<Character, State> other,
                                 IdentityHashMap<State, Integer> equalityReferenceMap) {
        if (mine.size() != other.size()) return false;
        Iterator otherEntrySet = other.entrySet().iterator();
        for (Map.Entry<Character, State> e : mine.entrySet()) {
            Map.Entry<Character, State> otherE = (Map.Entry<Character, State>) otherEntrySet.next();

            if (!e.getKey().equals(otherE.getKey())) return false;
            Integer reference = equalityReferenceMap.get(e.getValue());
            if (reference == null) {
                equalityReferenceMap.put(e.getValue(), equalityReferenceMap.size() + 1);
                if (!equals(e.getValue(), otherE.getValue(), equalityReferenceMap)) return false;
            }
        }
        return true;
    }

    private class StateObjectOutputStream extends ObjectOutputStream {

        private final IdentityHashMap<Object, Integer> objectToReference;
        private int referenceCount;

        StateObjectOutputStream(ObjectOutputStream out, IdentityHashMap<Object, Integer> objectToReference,
                                int referenceCount)
                throws IOException {
            super(out);
            this.objectToReference = objectToReference;
            this.referenceCount = referenceCount;
        }

        IdentityHashMap<Object, Integer> getObjectToReference() {
            return objectToReference;
        }

        int getReferenceCount() {
            return referenceCount;
        }

        int incrementAndGetReferenceCount() {
            referenceCount += 1;
            return referenceCount;
        }
    }

    class StateObjectInputStream extends ObjectInputStream {
        private final IdentityHashMap<Integer, Object> referenceToObject;

        StateObjectInputStream(ObjectInputStream in, IdentityHashMap<Integer, Object> referenceToObject)
                throws IOException {
            super(in);
            this.referenceToObject = referenceToObject;
        }

        IdentityHashMap<Integer, Object> getReferenceToObject() {
            return referenceToObject;
        }
    }
}
