package org.ahocorasick.trie.candidate;

import org.ahocorasick.trie.Emit;
import org.ahocorasick.trie.handler.EmitHandler;

public class EmitCandidateFlushHandler {

    private final EmitHandler emitHandler;

    private final EmitCandidateHolder emitCandidateHolder;

    public EmitCandidateFlushHandler(EmitHandler emitHandler, EmitCandidateHolder emitCandidateHolder) {
        this.emitHandler = emitHandler;
        this.emitCandidateHolder = emitCandidateHolder;
    }

    public void flush() {
        for (Emit emit : emitCandidateHolder.flush()) {
            emitHandler.emit(emit);
        }
    }

    public boolean stop() {
        return emitHandler.stop();
    }

}
