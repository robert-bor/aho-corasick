package org.ahocorasick.trie.handler;

import org.ahocorasick.trie.Emit;
import org.ahocorasick.trie.PayloadEmit;

public class PayloadEmitDelegateHandlerStateless implements PayloadEmitHandler<String> {

    private EmitHandler handler;

    public PayloadEmitDelegateHandlerStateless(EmitHandler handler) {
        this.handler = handler;

    }

    @Override
    public boolean emit(PayloadEmit<String> emit) {
        Emit newEmit = new Emit(emit.getStart(), emit.getEnd(), emit.getKeyword());
        return handler.emit(newEmit);
    }

}
