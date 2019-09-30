package com.nomad.model.server;

import com.nomad.exception.SystemException;
import com.nomad.message.RawMessage;

public interface CacheClient {
    RawMessage execMessage(final RawMessage message, final byte version) throws SystemException;

    void setShouldClose(final boolean shouldClose);

    String getId();

    void closeObject();

}
