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
    
    private interface KeywordTokenizer {
        public Transition nextTransition();
    }
    
    private class WordTokenizer implements KeywordTokenizer {
        private final java.util.StringTokenizer st;
        private boolean lastWasSpace = true;
        public WordTokenizer(String keyword) {
            st = new java.util.StringTokenizer(keyword);
        }
        @Override
        public Transition<String> nextTransition() {
            Transition t;
            if (lastWasSpace) {
                t = new Transition<>(st.nextToken());
            } else {
                t = new Transition<>(' ');
            }
            lastWasSpace = !lastWasSpace;
            return t;
        }
    }
    
    private class CharacterTokenizer implements KeywordTokenizer {
        private final java.text.StringCharacterIterator ct;
        public CharacterTokenizer(String keyword) {
            ct = new java.text.StringCharacterIterator(keyword);
        }
        @Override
        public Transition<Character> nextTransition() {
            return new Transition<>(ct.next());
        }
    }
    
    private KeywordTokenizer keywordTokenizer(String keyword) {
        KeywordTokenizer kwt;
        if (trieConfig.hasOnlyWordNodes()) {
            kwt = new WordTokenizer(keyword);
        }
        else {
            kwt = new CharacterTokenizer(keyword);
        }
        return kwt;
    }

    private class TokenStream {
        private final KeywordTokenizer kwt;
        private Transition lookahead;
        private final StringBuffer input = new StringBuffer();
        
        public TokenStream(KeywordTokenizer kwt) {
            this.kwt = kwt;
        }
        
        public Transition nextTransition() {
            Transition next = lookahead;
            if (next == null) {
                next = kwt.nextTransition();
            }
            else {
                lookahead = null;
            }
            if (next != null) {
                input.append(next.transitionToken().toString());
            }
            return next;
        }
        
        public int position() {
            return input.length();
        }
        
        public boolean isWholeWord(int start) {
            if (lookahead == null) {
                lookahead = kwt.nextTransition();
            }
            return ((start == 0 || 
                     Character.isSpaceChar(input.codePointAt(start))) && 
                    (lookahead == null || lookahead.isWordSeparator()));
        }
    }
        
    private void addKeyword(String keyword) {
        if (keyword == null || keyword.length() == 0) {
            return;
        }
        if (trieConfig.isCaseInsensitive()) {
            keyword = keyword.toLowerCase();
        }
        State currentState = this.rootState;
        KeywordTokenizer tknz = keywordTokenizer(keyword);
        Transition tn = tknz.nextTransition();
        while (tn != null) {
            currentState = currentState.addState(tn);
            tn = tknz.nextTransition();
        }
        currentState.addEmit(keyword);
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

        final EmitCandidateFlushHandler flushHandler = new EmitCandidateFlushHandler(emitHandler, emitCandidateHolder);

        String input = text.toString();
        if (trieConfig.isCaseInsensitive()) {
            input = input.toLowerCase();
        }
        TokenStream tknz = new TokenStream(keywordTokenizer(input));
        
        State currentState = this.rootState;
        Transition tn = tknz.nextTransition();
        while (tn != null) {
            currentState = getState(currentState, tn, flushHandler);
            
            Collection<String> emits = currentState.emit();
            for (String emit : emits) {
                int position = tknz.position();
                int start = tknz.position() - emit.length() + 1;
                if (!trieConfig.isOnlyWholeWords() || tknz.isWholeWord(start)) {
                    emitCandidateHolder.addCandidate(new Emit(start, position, emit));
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

        public Trie build() {
            trie.constructFailureStates();
            return trie;
        }
    }
}
