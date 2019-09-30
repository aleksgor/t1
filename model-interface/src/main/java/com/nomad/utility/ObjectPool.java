package com.nomad.utility;

import com.nomad.exception.LogicalException;
import com.nomad.exception.SystemException;

public interface ObjectPool<T extends PooledObject> {

    T getNewPooledObject() throws SystemException, LogicalException;

    String getPoolId();

    void freeObjects();

    T getObject();

    void registerRequest(long time, long size, long timeWait);

    void init() throws SystemException, LogicalException;

    void close();
}