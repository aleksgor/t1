package com.nomad.model;

public interface BlockInvoker {
    boolean softBlock(String sessionId, Identifier id);

    boolean hardBlock(String sessionId, Identifier id);

    void softUnlock(String sessionId, Identifier id);

    void hardUnlock(String sessionId, Identifier id);

    void cleanSession(String sessionId);

}
