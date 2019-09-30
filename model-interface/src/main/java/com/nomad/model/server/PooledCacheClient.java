package com.nomad.model.server;

import com.nomad.exception.SystemException;
import com.nomad.message.RawMessage;
import com.nomad.utility.PooledObject;

public interface PooledCacheClient extends CacheClient,PooledObject {
    @Override
    RawMessage execMessage(final RawMessage message, final byte version) throws SystemException;

    @Override
    void setShouldClose(final boolean shouldClose);

    @Override
    String getId();

    @Override
    void closeObject();

}
