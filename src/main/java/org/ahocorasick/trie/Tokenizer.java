package org.ahocorasick.trie;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Tokenizer {

    private final Collection<Emit> emits;

    private final String text;

    public Tokenizer(Collection<Emit> emits, String text) {
        this.emits = emits;
        this.text = text;
    }

    public Collection<Token> tokenize() {

        List<Token> tokens = new ArrayList<>();
        int lastCollectedPosition = -1;
        for (Emit emit : emits) {
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
        return new MatchToken(
                text.substring(emit.getStart(), emit.getEnd()+1),
                emit);
    }

}
