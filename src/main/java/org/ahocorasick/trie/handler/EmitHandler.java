package org.ahocorasick.trie.handler;

import org.ahocorasick.trie.Emit;

public interface EmitHandler {

    /**
     * Callback handler that deals with an emit it gets from the parser
     * @param emit the current emit that must be dealt with
     */
    void emit(Emit emit);

    /**
     * Force the parse process to stop
     * @return true if the process must stop
     */
    boolean stop();
}
