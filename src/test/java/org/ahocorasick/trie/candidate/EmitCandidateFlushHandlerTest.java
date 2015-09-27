package org.ahocorasick.trie.candidate;

import org.ahocorasick.trie.Emit;
import org.ahocorasick.trie.handler.EmitHandler;
import org.ahocorasick.trie.handler.FirstMatchHandler;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class EmitCandidateFlushHandlerTest {

    @Test
    public void stop() {
        EmitHandler emitHandler = new FirstMatchHandler();
        EmitCandidateFlushHandler flushHandler = new EmitCandidateFlushHandler(emitHandler, null);
        assertFalse(flushHandler.stop());
        emitHandler.emit(new Emit(0, 2, "bla"));
        assertTrue(flushHandler.stop());
    }
}
