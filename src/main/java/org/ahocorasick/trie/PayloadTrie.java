package org.ahocorasick.trie;

import static java.lang.Character.isWhitespace;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingDeque;

import org.ahocorasick.interval.IntervalTree;
import org.ahocorasick.interval.Intervalable;
import org.ahocorasick.trie.handler.DefaultPayloadEmitHandler;
import org.ahocorasick.trie.handler.PayloadEmitHandler;
import org.ahocorasick.trie.handler.StatefulPayloadEmitHandler;
import org.ahocorasick.util.ListElementRemoval;
import org.ahocorasick.util.ListElementRemoval.RemoveElementPredicate;

/**
 * A trie implementation that carries a payload. See {@link Trie} for
 * details on usage.
 *
 * <p>
 * The payload trie adds the possibility to specify emitted payloads for each
 * added keyword.
 * </p>
 * 
 * @author Daniel Beck
 * @param <T> The type of the supplied of the payload.
 */
public class PayloadTrie<T> {

    private final TrieConfig trieConfig;

    private final PayloadState<T> rootState;

    protected PayloadTrie(final TrieConfig trieConfig) {
        this.trieConfig = trieConfig;
        this.rootState = new PayloadState<>();
    }

    /**
     * Used by the builder to add a text search keyword with a emit payload.
     *
     * @param keyword The search term to add to the list of search terms.
     * @param emit    the payload to emit for this search term.
     * @throws NullPointerException if the keyword is null.
     */
    private void addKeyword(String keyword, T emit) {
        if (keyword.isEmpty()) {
            return;
        }

        if (isCaseInsensitive()) {
            keyword = keyword.toLowerCase();
        }

        addState(keyword).addEmit(new Payload<T>(keyword, emit));
    }

    /**
     * Used by the builder to add a text search keyword.
     *
     * @param keyword The search term to add to the list of search terms.
     * @throws NullPointerException if the keyword is null.
     */
    private void addKeyword(String keyword) {
        if (keyword.isEmpty()) {
            return;
        }

        if (isCaseInsensitive()) {
            keyword = keyword.toLowerCase();
        }

        addState(keyword).addEmit(new Payload<T>(keyword, null));
    }

    private PayloadState<T> addState(final String keyword) {
        return getRootState().addState(keyword);
    }

    /**
     * Tokenizes the specified text and returns the emitted outputs.
     * 
     * @param text The text to tokenize.
     * @return the emitted outputs
     */
    public Collection<PayloadToken<T>> tokenize(final String text) {
        final Collection<PayloadToken<T>> tokens = new ArrayList<>();
        final Collection<PayloadEmit<T>> collectedEmits = parseText(text);
        int lastCollectedPosition = -1;

        for (final PayloadEmit<T> emit : collectedEmits) {
            if (emit.getStart() - lastCollectedPosition > 1) {
                tokens.add((PayloadToken<T>) createFragment(emit, text, lastCollectedPosition));
            }

            tokens.add(createMatch(emit, text));
            lastCollectedPosition = emit.getEnd();
        }

        if (text.length() - lastCollectedPosition > 1) {
            tokens.add((PayloadToken<T>) createFragment(null, text, lastCollectedPosition));
        }

        return tokens;
    }

    private PayloadToken<T> createFragment(final PayloadEmit<T> emit, final String text, final int lastCollectedPosition) {
        return new PayloadFragmentToken<T>(
                text.substring(lastCollectedPosition + 1, emit == null ? text.length() : emit.getStart()));
    }

    private PayloadToken<T> createMatch(PayloadEmit<T> emit, String text) {
        return new PayloadMatchToken<T>(text.substring(emit.getStart(), emit.getEnd() + 1), emit);
    }

    /**
     * Tokenizes a specified text and returns the emitted outputs.
     * 
     * @param text The character sequence to tokenize.
     * @return A collection of emits.
     */
    public Collection<PayloadEmit<T>> parseText(final CharSequence text) {
        return parseText(text, new DefaultPayloadEmitHandler<T>());
    }

    /**
     * Tokenizes the specified text by using a custom EmitHandler and returns the
     * emitted outputs.
     * 
     * @param text        The character sequence to tokenize.
     * @param emitHandler The emit handler that will be used to parse the text.
     * @return A collection of emits.
     */
    @SuppressWarnings("unchecked")
    public Collection<PayloadEmit<T>> parseText(final CharSequence text, final StatefulPayloadEmitHandler<T> emitHandler) {
        parseText(text, (PayloadEmitHandler<T>) emitHandler);

        final List<PayloadEmit<T>> collectedEmits = emitHandler.getEmits();

        if (trieConfig.isOnlyWholeWords()) {
            removePartialMatches(text, collectedEmits);
        }

        if (trieConfig.isOnlyWholeWordsWhiteSpaceSeparated()) {
            removePartialMatchesWhiteSpaceSeparated(text, collectedEmits);
        }

        if (!trieConfig.isAllowOverlaps()) {
            IntervalTree intervalTree = new IntervalTree((List<Intervalable>) (List<?>) collectedEmits);
            intervalTree.removeOverlaps((List<Intervalable>) (List<?>) collectedEmits);
        }

        return collectedEmits;
    }

    /**
     * Returns true if the text contains contains one of the search terms. Else,
     * returns false.
     * 
     * @param text Specified text.
     * @return true if the text contains one of the search terms. Else, returns
     *         false.
     */
    public boolean containsMatch(final CharSequence text) {
        return firstMatch(text) != null;
    }

    /**
     * Tokenizes the specified text by using a custom EmitHandler and returns the
     * emitted outputs.
     * 
     * @param text        The character sequence to tokenize.
     * @param emitHandler The emit handler that will be used to parse the text.
     */
    public void parseText(final CharSequence text, final PayloadEmitHandler<T> emitHandler) {
        PayloadState<T> currentState = getRootState();

        for (int position = 0; position < text.length(); position++) {
            Character character = text.charAt(position);

            // TODO: Maybe lowercase the entire string at once?
            if (trieConfig.isCaseInsensitive()) {
                character = Character.toLowerCase(character);
            }

            currentState = getState(currentState, character);
            if (storeEmits(position, currentState, emitHandler) && trieConfig.isStopOnHit()) {
                return;
            }
        }
    }

    /**
     * The first matching text sequence.
     *
     * @param text The text to search for keywords.
     * @return null if no matches found.
     */
    public PayloadEmit<T> firstMatch(final CharSequence text) {
        if (!trieConfig.isAllowOverlaps()) {
            // Slow path. Needs to find all the matches to detect overlaps.
            final Collection<PayloadEmit<T>> parseText = parseText(text);

            if (parseText != null && !parseText.isEmpty()) {
                return parseText.iterator().next();
            }
        } else {
            // Fast path. Returns first match found.
            PayloadState<T> currentState = getRootState();

            for (int position = 0; position < text.length(); position++) {
                Character character = text.charAt(position);

                // TODO: Lowercase the entire string at once?
                if (trieConfig.isCaseInsensitive()) {
                    character = Character.toLowerCase(character);
                }

                currentState = getState(currentState, character);
                Collection<Payload<T>> payloads = currentState.emit();

                if (payloads != null && !payloads.isEmpty()) {
                    for (final Payload<T> payload : payloads) {
                        final PayloadEmit<T> emit = new PayloadEmit<>(position - payload.getKeyword().length() + 1, position,
                                payload.getKeyword(), payload.getData());
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

    private boolean isPartialMatch(final CharSequence searchText, final PayloadEmit<T> emit) {
        return (emit.getStart() != 0 && Character.isAlphabetic(searchText.charAt(emit.getStart() - 1)))
                || (emit.getEnd() + 1 != searchText.length() && Character.isAlphabetic(searchText.charAt(emit.getEnd() + 1)));
    }

    private void removePartialMatches(final CharSequence searchText, final List<PayloadEmit<T>> collectedEmits) {

        final RemoveElementPredicate<PayloadEmit<T>> predicate = new RemoveElementPredicate<PayloadEmit<T>>() {

            @Override
            public boolean remove(PayloadEmit<T> emit) {
                return isPartialMatch(searchText, emit);
            }

        };

        ListElementRemoval.removeIf(collectedEmits, predicate);
    }

    private void removePartialMatchesWhiteSpaceSeparated(final CharSequence searchText,
            final List<PayloadEmit<T>> collectedEmits) {
        final long size = searchText.length();
        final List<PayloadEmit<T>> removeEmits = new ArrayList<>();

        for (final PayloadEmit<T> emit : collectedEmits) {
            if ((emit.getStart() == 0 || isWhitespace(searchText.charAt(emit.getStart() - 1)))
                    && (emit.getEnd() + 1 == size || isWhitespace(searchText.charAt(emit.getEnd() + 1)))) {
                continue;
            }
            removeEmits.add(emit);
        }

        for (final PayloadEmit<T> removeEmit : removeEmits) {
            collectedEmits.remove(removeEmit);
        }
    }

    private PayloadState<T> getState(PayloadState<T> currentState, final Character character) {
        PayloadState<T> newCurrentState = currentState.nextState(character);

        while (newCurrentState == null) {
            currentState = currentState.failure();
            newCurrentState = currentState.nextState(character);
        }

        return newCurrentState;
    }

    private void constructFailureStates() {
        final Queue<PayloadState<T>> queue = new LinkedBlockingDeque<>();
        final PayloadState<T> startState = getRootState();

        // First, set the fail state of all depth 1 states to the root state
        for (PayloadState<T> depthOneState : startState.getStates()) {
            depthOneState.setFailure(startState);
            queue.add(depthOneState);
        }

        // Second, determine the fail state for all depth > 1 state
        while (!queue.isEmpty()) {
            final PayloadState<T> currentState = queue.remove();

            for (final Character transition : currentState.getTransitions()) {
                PayloadState<T> targetState = currentState.nextState(transition);
                queue.add(targetState);

                PayloadState<T> traceFailureState = currentState.failure();
                while (traceFailureState.nextState(transition) == null) {
                    traceFailureState = traceFailureState.failure();
                }

                final PayloadState<T> newFailureState = traceFailureState.nextState(transition);
                targetState.setFailure(newFailureState);
                targetState.addEmit(newFailureState.emit());
            }
        }
    }

    private boolean storeEmits(final int position, final PayloadState<T> currentState, final PayloadEmitHandler<T> emitHandler) {
        boolean emitted = false;
        final Collection<Payload<T>> payloads = currentState.emit();

        // TODO: The check for empty might be superfluous.
        if (payloads != null && !payloads.isEmpty()) {
            for (final Payload<T> payload : payloads) {
                emitted = emitHandler.emit(new PayloadEmit<T>(position - payload.getKeyword().length() + 1, position,
                        payload.getKeyword(), payload.getData())) || emitted;

                if (emitted && trieConfig.isStopOnHit()) {
                    break;
                }
            }
        }

        return emitted;
    }

    private boolean isCaseInsensitive() {
        return trieConfig.isCaseInsensitive();
    }

    private PayloadState<T> getRootState() {
        return this.rootState;
    }

    /**
     * Provides a fluent interface for constructing Trie instances with payloads.
     * @param <T> The type of the emitted payload.
     *
     * @return The builder used to configure its Trie.
     */
    public static <T> PayloadTrieBuilder<T> builder() {
        return new PayloadTrieBuilder<T>();
    }

    /**
     * Builder class to create a PayloadTrie instance.
     * 
     * @param <T> The type of the emitted payload.
     */
    public static class PayloadTrieBuilder<T> {

        private final TrieConfig trieConfig = new TrieConfig();

        private final PayloadTrie<T> trie = new PayloadTrie<>(trieConfig);

        /**
         * Default (empty) constructor.
         */
        private PayloadTrieBuilder() {
        }

        /**
         * Configure the Trie to ignore case when searching for keywords in the text.
         * This must be called before calling addKeyword because the algorithm converts
         * keywords to lowercase as they are added, depending on this case sensitivity
         * setting.
         *
         * @return This builder.
         */
        public PayloadTrieBuilder<T> ignoreCase() {
            this.trieConfig.setCaseInsensitive(true);
            return this;
        }

        /**
         * Configure the Trie to ignore overlapping keywords.
         *
         * @return This builder.
         */
        public PayloadTrieBuilder<T> ignoreOverlaps() {
            this.trieConfig.setAllowOverlaps(false);
            return this;
        }

        /**
         * Adds a keyword to the Trie's list of text search keywords. No Payload is
         * supplied.
         *
         * @param keyword The keyword to add to the list.
         * @return This builder.
         * @throws NullPointerException if the keyword is null.
         */
        public PayloadTrieBuilder<T> addKeyword(final String keyword) {
            this.trie.addKeyword(keyword);
            return this;
        }

        /**
         * Adds a keyword and a payload to the Trie's list of text search keywords.
         *
         * @param keyword The keyword to add to the list.
         * @param payload the payload to add
         * @return This builder.
         * @throws NullPointerException if the keyword is null.
         */
        public PayloadTrieBuilder<T> addKeyword(final String keyword, final T payload) {
            this.trie.addKeyword(keyword, payload);
            return this;
        }

        /**
         * Adds a list of keywords and payloads to the Trie's list of text search
         * keywords.
         *
         * @param keywords The keywords to add to the list.
         * @return This builder.
         */
        public PayloadTrieBuilder<T> addKeywords(final Collection<Payload<T>> keywords) {
            for (Payload<T> payload : keywords) {
                this.trie.addKeyword(payload.getKeyword(), payload.getData());
            }
            return this;
        }

        /**
         * Configure the Trie to match whole keywords in the text.
         *
         * @return This builder.
         */
        public PayloadTrieBuilder<T> onlyWholeWords() {
            this.trieConfig.setOnlyWholeWords(true);
            return this;
        }

        /**
         * Configure the Trie to match whole keywords that are separated by whitespace
         * in the text. For example, "this keyword thatkeyword" would only match the
         * first occurrence of "keyword".
         *
         * @return This builder.
         */
        public PayloadTrieBuilder<T> onlyWholeWordsWhiteSpaceSeparated() {
            this.trieConfig.setOnlyWholeWordsWhiteSpaceSeparated(true);
            return this;
        }

        /**
         * Configure the Trie to stop after the first keyword is found in the text.
         *
         * @return This builder.
         */
        public PayloadTrieBuilder<T> stopOnHit() {
            trie.trieConfig.setStopOnHit(true);
            return this;
        }

        /**
         * Configure the PayloadTrie based on the builder settings.
         *
         * @return The configured PayloadTrie.
         */
        public PayloadTrie<T> build() {
            this.trie.constructFailureStates();
            return this.trie;
        }

        /**
         * @return This builder.
         * @deprecated Use ignoreCase()
         */
        public PayloadTrieBuilder<T> caseInsensitive() {
            return ignoreCase();
        }

        /**
         * @return This builder.
         * @deprecated Use ignoreOverlaps()
         */
        public PayloadTrieBuilder<T> removeOverlaps() {
            return ignoreOverlaps();
        }
    }
}
