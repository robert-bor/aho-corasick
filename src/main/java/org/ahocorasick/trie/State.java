package org.ahocorasick.trie;

import org.ahocorasick.trie.candidate.EmitCandidateFlushHandler;

import java.util.*;

/**
 * <p>
 *     A state has various important tasks it must attend to:
 * </p>
 *
 * <ul>
 *     <li>success; when a transition points to another state, it must return that state</li>
 *     <li>failure; when a transition has no matching state, the algorithm must be able to fall back on a
 *         state with less depth</li>
 *     <li>emits; when this state is passed and keywords have been matched, the matches must be
 *         'emitted' so that they can be used later on.</li>
 * </ul>
 *
 * <p>
 *     The root state is special in the sense that it has no failure state; it cannot fail. If it 'fails'
 *     it will still parse the next transition and start from the root node. This ensures that the algorithm
 *     always runs. All other states always have a fail state.
 * </p>
 *
 * @author Robert Bor
 */
public class State {

    /** effectively the size of the keyword */
    private final int depth;

    /** only used for the root state to refer to itself in case no matches have been found */
    private final State rootState;

    /**
     * referred to in the white paper as the 'goto' structure. From a state it is possible to go
     * to other states, depending on the transition passed.
     */
    private final Map<Transition,State> success = new HashMap<>();

    /** if no matching states are found, the failure state will be returned */
    private State failure = null;

    /** whenever this state is reached, it will emit the matches keywords for future reference */
    private Set<Keyword> emits = null;

    public State() {
        this(0);
    }

    public State(int depth) {
        this.depth = depth;
        this.rootState = depth == 0 ? this : null;
    }

    private State nextState(final Transition transition, boolean ignoreRootState) {
        State nextState = this.success.get(transition);
        if (!ignoreRootState && nextState == null && this.rootState != null) {
            nextState = this.rootState;
        }
        return nextState;
    }

    public State nextState(final Transition transition) {
        return nextState(transition, false);
    }

    public State nextStateIgnoreRootState(final Transition transition) {
        return nextState(transition, true);
    }

    public State addState(final Transition transition) {
        State nextState = nextStateIgnoreRootState(transition);
        if (nextState == null) {
            nextState = new State(this.depth+1);
            this.success.put(transition, nextState);
        }
        return nextState;
    }

    public int getDepth() {
        return this.depth;
    }

    public void addEmit(final Keyword keyword) {
        if (this.emits == null) {
            this.emits = new TreeSet<>();
        }
        this.emits.add(keyword);
    }

    public void addEmit(final Collection<Keyword> emits) {
        for (Keyword emit : emits) {
            addEmit(emit);
        }
    }
    
    public void addEmitString(final String key) {
        addEmit(new Keyword(key, getDepth()));
    }

    public Collection<Keyword> emit() {
        return this.emits == null ? Collections.<Keyword> emptyList() : this.emits;
    }

    public State failure(final EmitCandidateFlushHandler emitCandidateFlushHandler) {
        if (emitCandidateFlushHandler != null && this.failure.isRootState()) {
            emitCandidateFlushHandler.flush();
        }
        return this.failure;
    }

    public State failure() {
        return failure(null);
    }

    public void setFailure(final State failState) {
        this.failure = failState;
    }

    public Collection<State> getStates() {
        return this.success.values();
    }

    public Collection<Transition> getTransitions() {
        return this.success.keySet();
    }

    public boolean isRootState() {
        return this.depth == 0;
    }

}
