package org.ahocorasick.trie;

import static java.lang.Character.isAlphabetic;
import static java.lang.Character.isWhitespace;
import static java.lang.Character.toLowerCase;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingDeque;
import org.ahocorasick.interval.IntervalTree;
import org.ahocorasick.interval.Intervalable;
import org.ahocorasick.trie.handler.DefaultEmitHandler;
import org.ahocorasick.trie.handler.EmitHandler;

/**
 * Based on the Aho-Corasick white paper, Bell technologies:
 * http://cr.yp.to/bib/1975/aho.pdf
 * 
 * @author Robert Bor
 */
public class Trie {

    private final TrieConfig trieConfig;

    private final State rootState;

    private Trie(final TrieConfig trieConfig) {
        this.trieConfig = trieConfig;
        this.rootState = new State();
    }
    
    /**
     * Used by the builder to add a text search keyword.
     * 
     * @param keyword The search term to add to the list of search terms.
     * 
     * @throws NullPointerException if the keyword is null.
     */
    private void addKeyword( String keyword ) {
        if( keyword.length() > 0 ) {
            if( isCaseInsensitive() ) {
                keyword = keyword.toLowerCase();
            }

            addState( keyword ).addEmit( keyword );
        }
    }

    private State addState( final String keyword ) {
        return getRootState().addState( keyword );
    }
    
    public Collection<Token> tokenize(final String text) {
        final Collection<Token> tokens = new ArrayList<>();
        final Collection<Emit> collectedEmits = parseText(text);
        int lastCollectedPosition = -1;
        
        for (final Emit emit : collectedEmits) {
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

    private Token createFragment(
        final Emit emit, 
        final String text,
        final int lastCollectedPosition) {
        return new FragmentToken(text.substring(lastCollectedPosition+1, emit == null ? text.length() : emit.getStart()));
    }

    private Token createMatch(final Emit emit, final String text) {
        return new MatchToken(text.substring(emit.getStart(), emit.getEnd()+1), emit);
    }

    @SuppressWarnings("unchecked")
    public Collection<Emit> parseText(final CharSequence text) {
        final DefaultEmitHandler emitHandler = new DefaultEmitHandler();
        parseText(text, emitHandler);

        final List<Emit> collectedEmits = emitHandler.getEmits();

        if (trieConfig.isOnlyWholeWords()) {
            removePartialMatches(text, collectedEmits);
        }

        if (trieConfig.isOnlyWholeWordsWhiteSpaceSeparated()) {
            removePartialMatchesWhiteSpaceSeparated(text, collectedEmits);
        }

        if (!trieConfig.isAllowOverlaps()) {
            IntervalTree intervalTree = new IntervalTree((List<Intervalable>)(List<?>)collectedEmits);
            intervalTree.removeOverlaps((List<Intervalable>) (List<?>) collectedEmits);
        }

        return collectedEmits;
    }

    public boolean containsMatch(final CharSequence text) {
        return firstMatch(text) != null;
    }

    public void parseText(final CharSequence text, final EmitHandler emitHandler) {
        State currentState = getRootState();
        
        for (int position = 0; position < text.length(); position++) {
            Character character = text.charAt(position);
            
            // TODO: Maybe lowercase the entire string at once?
            if (trieConfig.isCaseInsensitive()) {
                character = toLowerCase(character);
            }
            
            currentState = getState(currentState, character);
            if (storeEmits(position, currentState, emitHandler) && trieConfig.isStopOnHit()) {
                return;
            }
        }
    }

    public Emit firstMatch(final CharSequence text) {
        if (!trieConfig.isAllowOverlaps()) {
            // Slow path. Needs to find all the matches to detect overlaps.
            Collection<Emit> parseText = parseText(text);
            if (parseText != null && !parseText.isEmpty()) {
                return parseText.iterator().next();
            }
        } else {
            // Fast path. Returns first match found.
            State currentState = getRootState();
            
            for (int position = 0; position < text.length(); position++) {
                Character character = text.charAt(position);
                
                // TODO: Lowercase the entire string at once?
                if (trieConfig.isCaseInsensitive()) {
                    character = toLowerCase(character);
                }
                
                currentState = getState(currentState, character);
                Collection<String> emitStrs = currentState.emit();
                
                if (emitStrs != null && !emitStrs.isEmpty()) {
                    for (final String emitStr : emitStrs) {
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

    private boolean isPartialMatch(final CharSequence searchText, final Emit emit) {
        return (emit.getStart() != 0 &&
            isAlphabetic(searchText.charAt(emit.getStart() - 1))) ||
            (emit.getEnd() + 1 != searchText.length() &&
            isAlphabetic(searchText.charAt(emit.getEnd() + 1)));
    }

    private void removePartialMatches(final CharSequence searchText, final List<Emit> collectedEmits) {
        final List<Emit> removeEmits = new ArrayList<>();
        
        for (final Emit emit : collectedEmits) {
            if (isPartialMatch(searchText, emit)) {
                removeEmits.add(emit);
            }
        }
        
        for (final Emit removeEmit : removeEmits) {
            collectedEmits.remove(removeEmit);
        }
    }

    private void removePartialMatchesWhiteSpaceSeparated(final CharSequence searchText, final List<Emit> collectedEmits) {
        final long size = searchText.length();
        final List<Emit> removeEmits = new ArrayList<>();
        
        for (final Emit emit : collectedEmits) {
            if ((emit.getStart() == 0 || isWhitespace(searchText.charAt(emit.getStart() - 1))) &&
                (emit.getEnd() + 1 == size || isWhitespace(searchText.charAt(emit.getEnd() + 1)))) {
                continue;
            }
            removeEmits.add(emit);
        }
        
        for (final Emit removeEmit : removeEmits) {
            collectedEmits.remove(removeEmit);
        }
    }

    private State getState(final State initialState, final Character character) {
        State currentState = initialState;
        State updatedState = currentState.nextState(character);
        
        while (updatedState == null) {
            currentState = currentState.failure();
            updatedState = currentState.nextState(character);
        }
        
        return updatedState;
    }

    private void constructFailureStates() {
        final Queue<State> queue = new LinkedBlockingDeque<>();
        final State startState = getRootState();

        // First, set the fail state of all depth 1 states to the root state
        for (State depthOneState : startState.getStates()) {
            depthOneState.setFailure(startState);
            queue.add(depthOneState);
        }

        // Second, determine the fail state for all depth > 1 state
        while (!queue.isEmpty()) {
            final State currentState = queue.remove();

            for (final Character transition : currentState.getTransitions()) {
                State targetState = currentState.nextState(transition);
                queue.add(targetState);

                State traceFailureState = currentState.failure();
                while (traceFailureState.nextState(transition) == null) {
                    traceFailureState = traceFailureState.failure();
                }

                final State newFailureState = traceFailureState.nextState(transition);
                targetState.setFailure(newFailureState);
                targetState.addEmit(newFailureState.emit());
            }
        }
    }

    private boolean storeEmits(
        final int position,
        final State currentState,
        final EmitHandler emitHandler) {
        boolean emitted = false;
        final Collection<String> emits = currentState.emit();
        
        // TODO: The check for empty might be superfluous.
        if (emits != null && !emits.isEmpty()) {
            for (final String emit : emits) {
                emitHandler.emit(new Emit(position - emit.length() + 1, position, emit));
                emitted = true;
            }
        }
        
        return emitted;
    }

    private boolean isCaseInsensitive() {
      return trieConfig.isCaseInsensitive();
    }
    
    private State getRootState() {
      return this.rootState;
    }

    /**
     * Constructs a TrieBuilder instance for configuring the Trie using a fluent
     * interface.
     * 
     * @return The builder used to configure its Trie.
     */
    public static TrieBuilder builder() {
        return new TrieBuilder();
    }

    /**
     * Provides a fluent interface for constructing Trie instances.
     */
    public static class TrieBuilder {

        private final TrieConfig trieConfig = new TrieConfig();

        private final Trie trie = new Trie(trieConfig);

        /**
         * Default (empty) constructor.
         */
        private TrieBuilder() {}

        /**
         * Adds a keyword to the Trie's list of text search keywords.
         * 
         * @param keyword The keyword to add to the list.
         * 
         * @return This builder.
         * @throws NullPointerException if the keyword is null.
         */
        public TrieBuilder addKeyword(final CharSequence keyword) {
            getTrie().addKeyword( keyword.toString() );
            return this;
        }

        /**
         * Adds a list of keywords to the Trie's list of text search keywords.
         * 
         * @param keywords The keywords to add to the list.
         * 
         * @return This builder.
         */
        public TrieBuilder addKeywords(final CharSequence... keywords) {
            for( final CharSequence keyword : keywords ) {
                addKeyword( keyword );
            }

            return this;
        }

        /**
         * Adds a list of keywords to the Trie's list of text search keywords.
         * 
         * @param keywords The keywords to add to the list.
         * 
         * @return This builder.
         */
        public TrieBuilder addKeywords(final Collection<CharSequence> keywords) {
            return addKeywords( keywords.toArray( new CharSequence[ keywords.size() ] ) );
        }

        /**
         * Configure the Trie to ignore case when searching for keywords in the
         * text.
         * 
         * @return This builder.
         */
        public TrieBuilder ignoreCase() {
            getTrieConfig().setCaseInsensitive(true);
            return this;
        }

        /**
         * Configure the Trie to ignore overlapping keywords.
         * 
         * @return This builder.
         */
        public TrieBuilder ignoreOverlaps() {
            getTrieConfig().setAllowOverlaps(false);
            return this;
        }

        /**
         * Configure the Trie to match whole keywords in the text.
         * 
         * @return This builder.
         */
        public TrieBuilder onlyWholeWords() {
            getTrieConfig().setOnlyWholeWords(true);
            return this;
        }

        /**
         * Configure the Trie to match whole keywords that are separated by
         * whitespace in the text. For example, "this keyword thatkeyword"
         * would only match the first occurrence of "keyword".
         * 
         * @return This builder.
         */
        public TrieBuilder onlyWholeWordsWhiteSpaceSeparated() {
            getTrieConfig().setOnlyWholeWordsWhiteSpaceSeparated(true);
            return this;
        }
        
        /**
         * Configure the Trie to stop searching for matches after the first
         * keyword is found in the text.
         * 
         * @return This builder.
         */
        public TrieBuilder onlyFirstMatch() {
            getTrieConfig().setStopOnHit(true);
            return this;
        }

        /**
         * Construct the Trie using the builder settings.
         * 
         * @return The configured Trie.
         */
        public Trie build() {
            getTrie().constructFailureStates();
            return getTrie();
        }
        
        private Trie getTrie() {
            return this.trie;
        }
        
        private TrieConfig getTrieConfig() {
            return this.trieConfig;
        }
        
        /**
         * @deprecated Use onlyFirstMatch()
         */
        public TrieBuilder stopOnHit() {
          return onlyFirstMatch();
        }

        /**
         * @deprecated Use ignoreCase()
         */
        public TrieBuilder caseInsensitive() {
            return ignoreCase();
        }

        /**
         * @deprecated Use ignoreOverlaps()
         */
        public TrieBuilder removeOverlaps() {
            return ignoreOverlaps();
        }
    }
}
