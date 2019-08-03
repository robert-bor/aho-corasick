package org.ahocorasick.trie;

import org.ahocorasick.interval.IntervalTree;
import org.ahocorasick.interval.Intervalable;
import org.ahocorasick.trie.handler.DefaultEmitHandler;
import org.ahocorasick.trie.handler.EmitHandler;
import org.ahocorasick.trie.handler.StatefulEmitHandler;
import org.ahocorasick.util.ListElementRemoval;
import org.ahocorasick.util.ListElementRemoval.RemoveElementPredicate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingDeque;

import static java.lang.Character.isWhitespace;

/**
 * Based on the Aho-Corasick white paper, Bell technologies:
 * http://cr.yp.to/bib/1975/aho.pdf
 *
 * @author Robert Bor
 */
public class Trie<T> {

    private final TrieConfig trieConfig;

    private final State<T> rootState;

    private Trie(final TrieConfig trieConfig) {
        this.trieConfig = trieConfig;
        this.rootState = new State<>();
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

        addState(keyword).addEmit(keyword, null);
    }

    /**
     * Used by the builder to add a text search keyword with a payload.
     *
     * @param keyword The search term to add to the list of search terms.
     * @throws NullPointerException if the keyword is null.
     */
    private void addKeyword(String keyword, T payloadData) {
        if (keyword.isEmpty()) {
            return;
        }

        if (isCaseInsensitive()) {
            keyword = keyword.toLowerCase();
        }

        addState(keyword).addEmit(keyword, payloadData);
    }

    /**
     * Delegates to addKeyword.
     *
     * @param keywords List of search term to add to the list of search terms.
     */
    private void addKeywords(final String[] keywords) {
        for (final String keyword : keywords) {
            addKeyword(keyword);
        }
    }

    /**
     * Delegates to addKeyword.
     *
     * @param keywords List of search term to add to the list of search terms.
     */
    private void addKeywords(final Collection<String> keywords) {
        for (final String keyword : keywords) {
            addKeyword(keyword);
        }
    }

    private State<T> addState(final String keyword) {
        return getRootState().addState(keyword);
    }

    public Collection<Token> tokenize(final String text) {
        final Collection<Token> tokens = new ArrayList<>();
        final Collection<Emit<T>> collectedEmits = parseText(text);
        int lastCollectedPosition = -1;

        for (final Emit<T> emit : collectedEmits) {
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

    private Token createFragment(final Emit<T> emit, final String text, final int lastCollectedPosition) {
        return new FragmentToken(text.substring(lastCollectedPosition + 1, emit == null ? text.length() : emit.getStart()));
    }

    private Token createMatch(Emit<T> emit, String text) {
        return new MatchToken(text.substring(emit.getStart(), emit.getEnd() + 1), emit);
    }

    public Collection<Emit<T>> parseText(final CharSequence text) {
        return parseText(text, new DefaultEmitHandler<T>());
    }

    @SuppressWarnings("unchecked")
    public Collection<Emit<T>> parseText(final CharSequence text, final StatefulEmitHandler<T> emitHandler) {
        parseText(text, (EmitHandler<T>) emitHandler);

        final List<Emit<T>> collectedEmits = emitHandler.getEmits();

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

    public boolean containsMatch(final CharSequence text) {
        return firstMatch(text) != null;
    }

    public void parseText(final CharSequence text, final EmitHandler<T> emitHandler) {
        State<T> currentState = getRootState();

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
    public Emit<T> firstMatch(final CharSequence text) {
        if (!trieConfig.isAllowOverlaps()) {
            // Slow path. Needs to find all the matches to detect overlaps.
            final Collection<Emit<T>> parseText = parseText(text);

            if (parseText != null && !parseText.isEmpty()) {
                return parseText.iterator().next();
            }
        } else {
            // Fast path. Returns first match found.
            State<T> currentState = getRootState();

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
                        final Emit<T> emit = new Emit<>(position - payload.getKeyword().length() + 1, position, payload);
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

    private boolean isPartialMatch(final CharSequence searchText, final Emit<T> emit) {
        return (emit.getStart() != 0 && Character.isAlphabetic(searchText.charAt(emit.getStart() - 1)))
                || (emit.getEnd() + 1 != searchText.length() && Character.isAlphabetic(searchText.charAt(emit.getEnd() + 1)));
    }

    private void removePartialMatches(final CharSequence searchText, final List<Emit<T>> collectedEmits) {

        final RemoveElementPredicate<Emit<T>> predicate = new RemoveElementPredicate<Emit<T>>() {

            @Override
            public boolean remove(Emit<T> emit) {
                return isPartialMatch(searchText, emit);
            }

        };

        ListElementRemoval.removeIf(collectedEmits, predicate);
    }

    private void removePartialMatchesWhiteSpaceSeparated(final CharSequence searchText, final List<Emit<T>> collectedEmits) {
        final long size = searchText.length();
        final List<Emit<T>> removeEmits = new ArrayList<>();

        for (final Emit<T> emit : collectedEmits) {
            if ((emit.getStart() == 0 || isWhitespace(searchText.charAt(emit.getStart() - 1)))
                    && (emit.getEnd() + 1 == size || isWhitespace(searchText.charAt(emit.getEnd() + 1)))) {
                continue;
            }
            removeEmits.add(emit);
        }

        for (final Emit<T> removeEmit : removeEmits) {
            collectedEmits.remove(removeEmit);
        }
    }

    private State<T> getState(State<T> currentState, final Character character) {
        State<T> newCurrentState = currentState.nextState(character);

        while (newCurrentState == null) {
            currentState = currentState.failure();
            newCurrentState = currentState.nextState(character);
        }

        return newCurrentState;
    }

    private void constructFailureStates() {
        final Queue<State<T>> queue = new LinkedBlockingDeque<>();
        final State<T> startState = getRootState();

        // First, set the fail state of all depth 1 states to the root state
        for (State<T> depthOneState : startState.getStates()) {
            depthOneState.setFailure(startState);
            queue.add(depthOneState);
        }

        // Second, determine the fail state for all depth > 1 state
        while (!queue.isEmpty()) {
            final State<T> currentState = queue.remove();

            for (final Character transition : currentState.getTransitions()) {
                State<T> targetState = currentState.nextState(transition);
                queue.add(targetState);

                State<T> traceFailureState = currentState.failure();
                while (traceFailureState.nextState(transition) == null) {
                    traceFailureState = traceFailureState.failure();
                }

                final State<T> newFailureState = traceFailureState.nextState(transition);
                targetState.setFailure(newFailureState);
                targetState.addPayloadEmit(newFailureState.emit());
            }
        }
    }

    private boolean storeEmits(final int position, final State<T> currentState, final EmitHandler<T> emitHandler) {
        boolean emitted = false;
        final Collection<Payload<T>> payloads = currentState.emit();

        // TODO: The check for empty might be superfluous.
        if (payloads != null && !payloads.isEmpty()) {
            for (final Payload<T> payload : payloads) {
                emitted = emitHandler.emit(new Emit<T>(position - payload.getKeyword().length() + 1, position, payload))
                        || emitted;
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

    private State<T> getRootState() {
        return this.rootState;
    }

    /**
     * Provides a fluent interface for constructing Trie instances.
     *
     * @return The builder used to configure its Trie.
     */
    public static TrieBuilder builder() {
        return new TrieBuilder();
    }

    /**
     * Provides a fluent interface for constructing Trie instances.
     *
     * @return The builder used to configure its Trie.
     */
    public static <T> PayloadTrieBuilder<T> payloadBuilder() {
        return new PayloadTrieBuilder<T>();
    }

    public static class TrieBuilder {

        private final TrieConfig trieConfig = new TrieConfig();

        private final Trie<String> trie = new Trie<>(trieConfig);

        /**
         * Default (empty) constructor.
         */
        private TrieBuilder() {
        }

        /**
         * Configure the Trie to ignore case when searching for keywords in the text.
         * This must be called before calling addKeyword because the algorithm converts
         * keywords to lowercase as they are added, depending on this case sensitivity
         * setting.
         *
         * @return This builder.
         */
        public TrieBuilder ignoreCase() {
            this.trieConfig.setCaseInsensitive(true);
            return this;
        }

        /**
         * Configure the Trie to ignore overlapping keywords.
         *
         * @return This builder.
         */
        public TrieBuilder ignoreOverlaps() {
            this.trieConfig.setAllowOverlaps(false);
            return this;
        }

        /**
         * Adds a keyword to the Trie's list of text search keywords.
         *
         * @param keyword The keyword to add to the list.
         * @return This builder.
         * @throws NullPointerException if the keyword is null.
         */
        public TrieBuilder addKeyword(final String keyword) {
            this.trie.addKeyword(keyword);
            return this;
        }

        /**
         * Adds a list of keywords to the Trie's list of text search keywords.
         *
         * @param keywords The keywords to add to the list.
         * @return This builder.
         */
        public TrieBuilder addKeywords(final String... keywords) {
            this.trie.addKeywords(keywords);
            return this;
        }

        /**
         * Adds a list of keywords to the Trie's list of text search keywords.
         *
         * @param keywords The keywords to add to the list.
         * @return This builder.
         */
        public TrieBuilder addKeywords(final Collection<String> keywords) {
            this.trie.addKeywords(keywords);
            return this;
        }

        /**
         * Configure the Trie to match whole keywords in the text.
         *
         * @return This builder.
         */
        public TrieBuilder onlyWholeWords() {
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
        public TrieBuilder onlyWholeWordsWhiteSpaceSeparated() {
            this.trieConfig.setOnlyWholeWordsWhiteSpaceSeparated(true);
            return this;
        }

        /**
         * Configure the Trie to stop after the first keyword is found in the text.
         *
         * @return This builder.
         */
        public TrieBuilder stopOnHit() {
            trie.trieConfig.setStopOnHit(true);
            return this;
        }

        /**
         * Configure the Trie based on the builder settings.
         *
         * @return The configured Trie.
         */
        public Trie<String> build() {
            this.trie.constructFailureStates();
            return this.trie;
        }

        /**
         * @return This builder.
         * @deprecated Use ignoreCase()
         */
        public TrieBuilder caseInsensitive() {
            return ignoreCase();
        }

        /**
         * @return This builder.
         * @deprecated Use ignoreOverlaps()
         */
        public TrieBuilder removeOverlaps() {
            return ignoreOverlaps();
        }
    }

    public static class PayloadTrieBuilder<T> {

        private final TrieConfig trieConfig = new TrieConfig();

        private final Trie<T> trie = new Trie<>(trieConfig);

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
         * Adds a keyword to the Trie's list of text search keywords.
         *
         * @param keyword The keyword to add to the list.
         * @return This builder.
         * @throws NullPointerException if the keyword is null.
         */
        public PayloadTrieBuilder<T> addKeyword(final String keyword, T payload) {
            this.trie.addKeyword(keyword, payload);
            return this;
        }

        /**
         * Adds a list of keywords to the Trie's list of text search keywords.
         *
         * @param keywords The keywords to add to the list.
         * @return This builder.
         */
        public PayloadTrieBuilder<T> addKeywords(final Payload<T>... payloads) {
            for (Payload<T> payload : payloads) {
                this.trie.addKeyword(payload.getKeyword(), payload.getData());
            }
            return this;
        }

        /**
         * Adds a list of keywords to the Trie's list of text search keywords.
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
         * Configure the Trie based on the builder settings.
         *
         * @return The configured Trie.
         */
        public Trie<T> build() {
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
