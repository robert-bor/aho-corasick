package org.ahocorasick.trie;

import org.ahocorasick.interval.IntervalTree;
import org.ahocorasick.trie.handler.DefaultEmitHandler;
import org.ahocorasick.trie.handler.EmitHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingDeque;

/**
 *
 * Based on the Aho-Corasick white paper, Bell technologies: http://cr.yp.to/bib/1975/aho.pdf
 * @author Robert Bor, Keith Player
 */
public class Trie {

    private TrieConfig trieConfig;

    private State rootState;

    private StreamingDataStore streamingDataStore = null;

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
            if (trieConfig.isCaseInsensitive()) {
                character = Character.toLowerCase(character);
            }
            currentState = currentState.addState(character);
        }
        currentState.addEmit(trieConfig.isCaseInsensitive() ? keyword.toLowerCase() : keyword);
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

    /**
     * Parse through a char sequence.
     *
     * @param text a char sequence.
     * @return the collection of emits.
     */
    public Collection<Emit> parseText(CharSequence text) {
        DefaultEmitHandler emitHandler = new DefaultEmitHandler();
        parseText(text, emitHandler);

        List<Emit> collectedEmits = emitHandler.getEmits();

        if (trieConfig.isOnlyWholeWords()) {
            removePartialMatches(text, collectedEmits);
        }

        if (trieConfig.isOnlyWholeWordsWhiteSpaceSeparated()) {
            removePartialMatchesWhiteSpaceSeparated(text, collectedEmits);
        }

        if (!trieConfig.isAllowOverlaps()) {
            IntervalTree intervalTree = new IntervalTree(collectedEmits);
            intervalTree.removeOverlaps(collectedEmits);
        }

        return collectedEmits;
    }

	public boolean containsMatch(CharSequence text) {
		Emit firstMatch = firstMatch(text);
		return firstMatch != null;
	}

    /**
     * Setup the emit collector.
     */
    public void setupEmitCollector()
    {
        streamingDataStore = new StreamingDataStore(this.rootState, 2000);
    }


    /**
     * This method allows an external class to stream data directly through the {@code Trie}.  This makes it efficient
     * to process through multiple {@code Trie}s at the same time while only reading over a file stream once.
     * {@see parseStreamingText} for an example pushing to a single {@code Trie}.
     *
     * @param characterInt the current character.
     * @param nextCharacter the next character.
     * @return whether an end state has occurred.
     * @throws IOException
     */
    public boolean pushToEmitCollector(int characterInt, int nextCharacter) throws IOException
    {
        RecentInputBuffer recentInputBuffer = streamingDataStore.getRecentInputBuffer();
        recentInputBuffer.add(characterInt);
        char character = (char) characterInt;

        if (trieConfig.isCaseInsensitive()) {
            character = Character.toLowerCase(character);
        }

        State currentState = getState(streamingDataStore.getCurrentState(), character);
        // isStopOnHit stops processing as soon as the first hit is found.
        if (storeValidatedEmits(streamingDataStore.getPosition(), currentState, streamingDataStore.getEmitHandler(), recentInputBuffer, (char) nextCharacter) && trieConfig.isStopOnHit()) {
            return true;
        }

        streamingDataStore.setCurrentState(currentState);
        streamingDataStore.incrementPosition();
        return false;
    }

    /**
     * Returns the collection of emits based on the Trie configuration.
     * @return the collection of emits.
     */
    public Collection<Emit> getEmitCollection() {
        if (streamingDataStore == null) {
            throw new IllegalStateException("getEmitCollection called before setupEmitCollector.");
        }

        List<Emit> collectedEmits = streamingDataStore.getEmitHandler().getEmits();

        if (!trieConfig.isAllowOverlaps()) {
            IntervalTree intervalTree = new IntervalTree(collectedEmits);
            intervalTree.removeOverlaps(collectedEmits);
        }

        return collectedEmits;
    }

    /**
     * Parses the text in a single sweep removing partial matches as specified.
     *
     * @param textReader the text reader.
     */
    public Collection<Emit> parseStreamingText(Reader textReader) throws IOException
    {
        setupEmitCollector();
        try (BufferedReader bufferedReader = new BufferedReader(textReader))
        {
            int currentRead = bufferedReader.read();
            while (currentRead > 0)
            {
                int nextCharacter = bufferedReader.read();
                pushToEmitCollector(currentRead, nextCharacter);
                currentRead = nextCharacter;
            }
        }
        return getEmitCollection();
    }


    /**
     * Parses the text in a single sweep removing partial matches if specified.
     *
     * @param textReader the text reader.
     */
    public Collection<Emit> parseText(Reader textReader) throws IOException
    {
        DefaultEmitHandler emitHandler = new DefaultEmitHandler();
        ///
        State currentState = this.rootState;

        int position = 0;
        RecentInputBuffer recentInputBuffer = new RecentInputBuffer(2000);
        try (BufferedReader bufferedReader = new BufferedReader(textReader))
        {
            int currentRead = bufferedReader.read();
            while (currentRead > 0)
            {
                recentInputBuffer.add(currentRead);
                char character = (char) currentRead;

                if (trieConfig.isCaseInsensitive()) {
                    character = Character.toLowerCase(character);
                }
                currentState = getState(currentState, character);
                int nextCharacter = bufferedReader.read();
                // isStopOnHit stops processing as soon as the first hit is found.
                if (storeValidatedEmits(position, currentState, emitHandler, recentInputBuffer, (char) nextCharacter) && trieConfig.isStopOnHit()) {
                    break;
                }

                position++;
                currentRead = nextCharacter;
            }
        }

        List<Emit> collectedEmits = emitHandler.getEmits();

        if (!trieConfig.isAllowOverlaps()) {
            IntervalTree intervalTree = new IntervalTree(collectedEmits);
            intervalTree.removeOverlaps(collectedEmits);
        }

        return collectedEmits;
    }

    /**
     * Parse the text for a char sequence.
     * @param text the char sequence.
     * @param emitHandler the emit handler.
     */
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

    /**
     * Returns {@code true} if the current emit is white space separated.
     *
     * @param recentInputBuffer the recent input buffer.
     * @param emit the emit.
     * @param nextChar the next char after the emit.
     * @return {@code true} if the current emit is white space separated.
     */
    private boolean isWhiteSpaceSeparated(RecentInputBuffer recentInputBuffer, Emit emit, int nextChar)
    {
        // We get the character before the emit using the {@code recentInputBuffer}.
        return ((emit.getStart() == 0 || Character.isWhitespace(recentInputBuffer.getByFilePosition(emit.getStart() - 1))) &&
                (nextChar == -1 || Character.isWhitespace(nextChar)));
    }

    /**
     * Returns {@code true} if the current emit is only a partial match (e.g. cat inside catastrophe).
     *
     * @param recentInputBuffer the recent input buffer.
     * @param emit the emit.
     * @param nextChar the next char after the emit.
     * @return {@code true} if the current emit is only a partial match (e.g. cat inside catastrophe).
     */
    private boolean isPartialMatch(RecentInputBuffer recentInputBuffer, Emit emit, int nextChar) {
        // We get the character before the emit using the {@code recentInputBuffer}.
        return (emit.getStart() != 0 &&
                Character.isLetterOrDigit(recentInputBuffer.getByFilePosition(emit.getStart() - 1))) ||
                (nextChar != -1 && Character.isLetterOrDigit(nextChar));
    }

    private boolean isPartialMatch(CharSequence searchText, Emit emit) {
		return (emit.getStart() != 0 &&
			Character.isLetterOrDigit(searchText.charAt(emit.getStart() - 1))) ||
			(emit.getEnd() + 1 != searchText.length() &&
			Character.isLetterOrDigit(searchText.charAt(emit.getEnd() + 1)));
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

    private void removePartialMatchesWhiteSpaceSeparated(CharSequence searchText, List<Emit> collectedEmits) {
        long size = searchText.length();
        List<Emit> removeEmits = new ArrayList<>();
        for (Emit emit : collectedEmits) {
            if ((emit.getStart() == 0 || Character.isWhitespace(searchText.charAt(emit.getStart() - 1))) &&
                (emit.getEnd() + 1 == size || Character.isWhitespace(searchText.charAt(emit.getEnd() + 1)))) {
                continue;
            }
            removeEmits.add(emit);
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

    /**
     * Stores emits if they pass the required validation steps.
     *
     * @param position the current position.
     * @param currentState the current state.
     * @param emitHandler the emit handler.
     * @param recentInputBuffer the recent input buffer used to retrieve characters before and after the emit.
     * @param nextChar the next char after the emit.
     * @return whether an emit was stored.
     */
    private boolean storeValidatedEmits(int position, State currentState, EmitHandler emitHandler, RecentInputBuffer recentInputBuffer, char nextChar) {
        boolean emitted = false;
        Collection<String> emits = currentState.emit();
        if (emits != null && !emits.isEmpty()) {
            for (String emitString : emits) {
                String originalString = recentInputBuffer.substringByFilePosition(position - emitString.length() + 1, position + 1);
                Emit emit = new Emit(position - emitString.length() + 1, position, originalString);
                if (trieConfig.isOnlyWholeWords()) {
                    if (isPartialMatch(recentInputBuffer, emit, nextChar)) {
                        continue;
                    }
                }

                if (trieConfig.isOnlyWholeWordsWhiteSpaceSeparated()) {
                    if (!isWhiteSpaceSeparated(recentInputBuffer, emit, nextChar)) {
                        continue;
                    }
                }

                emitHandler.emit(emit);
                emitted = true;
            }
        }
        return emitted;
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

    /**
     * Gets a new trie builder.
     *
     * @return a new trie builder.
     */
    public static TrieBuilder builder() {
        return new TrieBuilder();
    }

    /**
     * Holds the data necessary to process streaming data.
     */
    private static class StreamingDataStore {
        private State currentState;
        private int position;
        private RecentInputBuffer recentInputBuffer;
        private DefaultEmitHandler emitHandler = new DefaultEmitHandler();

        public StreamingDataStore(State rootState, int recentInputBufferSize)
        {
            currentState = rootState;
            position = 0;
            recentInputBuffer = new RecentInputBuffer(recentInputBufferSize);
        }

        public State getCurrentState() {
            return currentState;
        }

        public void setCurrentState(State currentState) {
            this.currentState = currentState;
        }

        public int getPosition() {
            return position;
        }

        public void incrementPosition() {
            position++;
        }

        public RecentInputBuffer getRecentInputBuffer() {
            return recentInputBuffer;
        }

        public DefaultEmitHandler getEmitHandler() {
            return emitHandler;
        }
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

        public TrieBuilder onlyWholeWordsWhiteSpaceSeparated() {
            this.trieConfig.setOnlyWholeWordsWhiteSpaceSeparated(true);
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
