package org.ahocorasick.trie;

import org.ahocorasick.trie.candidate.EmitCandidateFlushHandler;
import org.ahocorasick.trie.candidate.EmitCandidateHolder;
import org.ahocorasick.trie.candidate.NonOverlappingEmitCandidateHolder;
import org.ahocorasick.trie.candidate.OverlappingEmitCandidateHolder;
import org.ahocorasick.trie.handler.DefaultEmitHandler;
import org.ahocorasick.trie.handler.EmitHandler;
import org.ahocorasick.trie.handler.FirstMatchHandler;

import java.util.Collection;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingDeque;

/**
 *
 * Based on the Aho-Corasick white paper, Bell technologies: http://cr.yp.to/bib/1975/aho.pdf
 * @author Robert Bor
 */
public class Trie {

    private final TrieConfig trieConfig;

    private final State rootState;

    private Trie(TrieConfig trieConfig) {
        this.trieConfig = trieConfig;
        this.rootState = new State();
    }
    
    private abstract class KeywordTokenizer {
        protected int position = 0;
        protected CharSequence input;
        protected int length;
        protected KeywordTokenizer(CharSequence input) {
            this.input = input;
            this.length = input.length();
        }
        protected char currentChar() {
            return (position < length) ? input.charAt(position) : '\0';
        }
        public abstract Transition nextTransition();
        public int getPosition() {
            return position;
        }
    }
    
    private class WordTokenizer extends KeywordTokenizer {
        public WordTokenizer(CharSequence input) {
            super(input);
        }
        @Override
        public Transition<String> nextTransition() {
            WordTransition t = null;
            while (position < length && Character.isWhitespace(currentChar())) {
                ++position;
            }
            int start = position;
            if (start < length) {
                while (position < length && !Character.isWhitespace(currentChar())) {
                    ++position;
                }
                String word = input.subSequence(start, position).toString();
                t = new WordTransition(word, start);
            }
            return t;
        }
    }
    
    private class CharacterTokenizer extends KeywordTokenizer {
        public CharacterTokenizer(CharSequence input) {
            super(input);
        }
        @Override
        public Transition<Character> nextTransition() {
            CharacterTransition t = null;
            if (position < length) {
                t = new CharacterTransition(currentChar(), position);
                position += 1;
            }
            return t;
        }
    }
    
    private class TokenStream {
        private final KeywordTokenizer kwt;
        private final StringBuilder input;
        private Transition lookahead;
        
        public TokenStream(CharSequence text) {
            input = new StringBuilder(text.length());
            for (int p = 0; p < text.length(); ++p) {
                char ch = text.charAt(p);
                input.append(trieConfig.isCaseInsensitive() ?
                        Character.toLowerCase(ch) : ch);
            }
            if (trieConfig.hasWordTransitions()) {
                kwt = new WordTokenizer(input);
            }
            else {
                kwt = new CharacterTokenizer(input);
            }
            lookahead = null;
        }
        
        public Transition nextTransition() {
            Transition next = lookahead;
            if (next == null) {
                next = kwt.nextTransition();
            }
            else {
                lookahead = null;
            }
            return next;
        }
        
        public boolean isWholeWord(int start) {
            if (lookahead == null) {
                lookahead = kwt.nextTransition();
            }
            return ((start == 0 || 
                     Character.isWhitespace(input.charAt(start-1))) && 
                    (lookahead == null || lookahead.isWordSeparator()));
        }
        
        public String input() {
            return input.toString();
        }
    }
        
    private void addKeyword(CharSequence keyword) {
        if (keyword == null || keyword.length() == 0) {
            return;
        }
        State currentState = this.rootState;
        TokenStream tknz = new TokenStream(keyword);
        Transition tn = tknz.nextTransition();
        while (tn != null) {
            currentState = currentState.addState(tn);
            tn = tknz.nextTransition();
        }
        currentState.addEmit(tknz.input());
    }

    public Collection<Token> tokenize(String text) {
        return new Tokenizer(parseText(text), text).tokenize();
    }

    @SuppressWarnings("unchecked")
    public Collection<Emit> parseText(CharSequence text) {
        DefaultEmitHandler emitHandler = new DefaultEmitHandler();
        parseText(text, emitHandler);
        return emitHandler.getEmits();
    }

    public boolean containsMatch(CharSequence text) {
            Emit firstMatch = firstMatch(text);
            return firstMatch != null;
    }

    public Emit firstMatch(CharSequence text) {
        FirstMatchHandler emitHandler = new FirstMatchHandler();
        parseText(text, emitHandler);
        return emitHandler.getFirstMatch();
    }

    public void parseText(CharSequence text, EmitHandler emitHandler) {

        final EmitCandidateHolder emitCandidateHolder = this.trieConfig.isAllowOverlaps() ?
                new OverlappingEmitCandidateHolder() :
                new NonOverlappingEmitCandidateHolder();

        final EmitCandidateFlushHandler flushHandler = 
                new EmitCandidateFlushHandler(emitHandler, emitCandidateHolder);

        TokenStream tknz = new TokenStream(text);
        
        State currentState = this.rootState;
        Transition tn = tknz.nextTransition();
        while (tn != null) {
            if (flushHandler.stop()) {
                return;
            }
            currentState = getState(currentState, tn, flushHandler);
            Collection<String> emits = currentState.emit();
            for (String emit : emits) {
                int position = tn.getStart() + tn.getLength();
                int start = position - emit.length();
                boolean isWholeWord = tknz.isWholeWord(start);
                if (isWholeWord || !trieConfig.isOnlyWholeWords()) {
                    emitCandidateHolder.addCandidate(
                            new Emit(start, position - 1, emit, isWholeWord));
                }
            }
            tn = tknz.nextTransition();
        }
        flushHandler.flush();
    }

    private State getState(State currentState, Transition transition, EmitCandidateFlushHandler flushHandler) {
        State newCurrentState = currentState.nextState(transition);
        while (newCurrentState == null) {
            currentState = currentState.failure(flushHandler);
            newCurrentState = currentState.nextState(transition);
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

            for (Transition transition : currentState.getTransitions()) {
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

    public static TrieBuilder builder() {
        return new TrieBuilder();
    }

    public static class TrieBuilder {

        private final TrieConfig trieConfig = new TrieConfig();

        private final Trie trie = new Trie(trieConfig);
        
        private boolean hasAddedKeyword = false;

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

        public TrieBuilder wordTransitions() {
            if (hasAddedKeyword) {
                throw new IllegalStateException(
                    "Unable to switch to word transitions after keywords added");
            }
            this.trieConfig.setWordTransitions(true);
            return this;
        }

        public TrieBuilder addKeyword(String keyword) {
            trie.addKeyword(keyword);
            hasAddedKeyword = true;
            return this;
        }
        
        public Trie build() {
            trie.constructFailureStates();
            return trie;
        }
    }
}
