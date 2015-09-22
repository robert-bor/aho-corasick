package org.ahocorasick.trie;

import org.ahocorasick.interval.IntervalTree;
import org.ahocorasick.interval.Intervalable;
import org.ahocorasick.trie.handler.DefaultEmitHandler;
import org.ahocorasick.trie.handler.EmitHandler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingDeque;

/**
 *
 * Based on the Aho-Corasick white paper, Bell technologies: http://cr.yp.to/bib/1975/aho.pdf
 * @author Robert Bor
 */
public class Trie {

    private TrieConfig trieConfig;

    private State rootState;

    private Trie(TrieConfig trieConfig) {
        this.trieConfig = trieConfig;
        this.rootState = new State();
    }

    private void addKeyword(String keyword) {
        if (keyword == null || keyword.length() == 0) {
            return;
        }
        State currentState = this.rootState;
        for (Character character : keyword.toCharArray()) {
            currentState = currentState.addState(character);
        }
        currentState.addEmit(keyword);
    }

    public Collection<Token> tokenize(String text) {

        Collection<Token> tokens = new ArrayList<>();

        Collection<Emit> collectedEmits = parseText(text);
        int lastCollectedPosition = -1;
        for (Emit emit : collectedEmits) {
            if (emit.getStart() - lastCollectedPosition > 1) {
                tokens.add(createFragment(emit, text, lastCollectedPosition));
            }
            tokens.add(createMatch(emit, text));
            lastCollectedPosition = emit.getEnd();
        }
        if (text.length() - lastCollectedPosition > 1) {
            tokens.add(createFragment(null, text, lastCollectedPosition));
        }

        return tokens;
    }

    private Token createFragment(Emit emit, String text, int lastCollectedPosition) {
        return new FragmentToken(text.substring(lastCollectedPosition+1, emit == null ? text.length() : emit.getStart()));
    }

    private Token createMatch(Emit emit, String text) {
        return new MatchToken(text.substring(emit.getStart(), emit.getEnd()+1), emit);
    }

    @SuppressWarnings("unchecked")
    public Collection<Emit> parseText(CharSequence text) {
        DefaultEmitHandler emitHandler = new DefaultEmitHandler();
        parseText(text, emitHandler);

        List<Emit> collectedEmits = emitHandler.getEmits();

        if (trieConfig.isOnlyWholeWords()) {
            removePartialMatches(text, collectedEmits);
        }

        if (!trieConfig.isAllowOverlaps()) {
            IntervalTree intervalTree = new IntervalTree((List<Intervalable>)(List<?>)collectedEmits);
            intervalTree.removeOverlaps((List<Intervalable>) (List<?>) collectedEmits);
        }

        return collectedEmits;
    }

	public boolean containsMatch(CharSequence text) {
		Emit firstMatch = firstMatch(text);
		return firstMatch != null;
	}

    public void parseText(CharSequence text, EmitHandler emitHandler) {
        State currentState = this.rootState;
        for (int position = 0; position < text.length(); position++) {
            Character character = text.charAt(position);
            if (trieConfig.isCaseInsensitive()) {
                character = Character.toLowerCase(character);
            }
            currentState = getState(currentState, character);
            if (storeEmits(position, currentState, emitHandler) && trieConfig.isStopOnHit()) {
                return;
            }
        }

    }

	public Emit firstMatch(CharSequence text) {
		if (!trieConfig.isAllowOverlaps()) {
			// Slow path. Needs to find all the matches to detect overlaps.
			Collection<Emit> parseText = parseText(text);
			if (parseText != null && !parseText.isEmpty()) {
				return parseText.iterator().next();
			}
		} else {
			// Fast path. Returns first match found.
			State currentState = this.rootState;
            for (int position = 0; position < text.length(); position++) {
                Character character = text.charAt(position);
				if (trieConfig.isCaseInsensitive()) {
					character = Character.toLowerCase(character);
				}
				currentState = getState(currentState, character);
				Collection<String> emitStrs = currentState.emit();
				if (emitStrs != null && !emitStrs.isEmpty()) {
					for (String emitStr : emitStrs) {
						final Emit emit = new Emit(position - emitStr.length() + 1, position, emitStr);
						if (trieConfig.isOnlyWholeWords()) {
							if (!isPartialMatch(text, emit)) {
								return emit;
							}
						} else {
							return emit;
						}
					}
				}
			}
		}
		return null;
	}

	private boolean isPartialMatch(CharSequence searchText, Emit emit) {
		return (emit.getStart() != 0 &&
			Character.isAlphabetic(searchText.charAt(emit.getStart() - 1))) ||
			(emit.getEnd() + 1 != searchText.length() &&
			Character.isAlphabetic(searchText.charAt(emit.getEnd() + 1)));
	}

	private void removePartialMatches(CharSequence searchText, List<Emit> collectedEmits) {
		List<Emit> removeEmits = new ArrayList<>();
		for (Emit emit : collectedEmits) {
			if (isPartialMatch(searchText, emit)) {
				removeEmits.add(emit);
			}
		}
		for (Emit removeEmit : removeEmits) {
			collectedEmits.remove(removeEmit);
		}
	}

    private State getState(State currentState, Character character) {
        State newCurrentState = currentState.nextState(character);
        while (newCurrentState == null) {
            currentState = currentState.failure();
            newCurrentState = currentState.nextState(character);
        }
        return newCurrentState;
    }

    private void constructFailureStates() {
        Queue<State> queue = new LinkedBlockingDeque<>();

        // First, set the fail state of all depth 1 states to the root state
        for (State depthOneState : this.rootState.getStates()) {
            depthOneState.setFailure(this.rootState);
            queue.add(depthOneState);
        }

        // Second, determine the fail state for all depth > 1 state
        while (!queue.isEmpty()) {
            State currentState = queue.remove();

            for (Character transition : currentState.getTransitions()) {
                State targetState = currentState.nextState(transition);
                queue.add(targetState);

                State traceFailureState = currentState.failure();
                while (traceFailureState.nextState(transition) == null) {
                    traceFailureState = traceFailureState.failure();
                }
                State newFailureState = traceFailureState.nextState(transition);
                targetState.setFailure(newFailureState);
                targetState.addEmit(newFailureState.emit());
            }
        }
    }

    private boolean storeEmits(int position, State currentState, EmitHandler emitHandler) {
        boolean emitted = false;
        Collection<String> emits = currentState.emit();
        if (emits != null && !emits.isEmpty()) {
            for (String emit : emits) {
                emitHandler.emit(new Emit(position - emit.length() + 1, position, emit));
                emitted = true;
            }
        }
        return emitted;
    }

    public static TrieBuilder builder() {
        return new TrieBuilder();
    }

    public static class TrieBuilder {

        private TrieConfig trieConfig = new TrieConfig();

        private Trie trie = new Trie(trieConfig);

        private TrieBuilder() {}

        public TrieBuilder caseInsensitive() {
            this.trieConfig.setCaseInsensitive(true);
            return this;
        }

        public TrieBuilder removeOverlaps() {
            this.trieConfig.setAllowOverlaps(false);
            return this;
        }

        public TrieBuilder onlyWholeWords() {
            this.trieConfig.setOnlyWholeWords(true);
            return this;
        }

        public TrieBuilder addKeyword(String keyword) {
            trie.addKeyword(keyword);
            return this;
        }

        public TrieBuilder stopOnHit() {
            trie.trieConfig.setStopOnHit(true);
            return this;
        }

        public Trie build() {
            trie.constructFailureStates();
            return trie;
        }
    }
}
