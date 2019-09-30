package com.nomad.server;

import com.nomad.utility.ObjectPool;
import com.nomad.utility.PooledObject;

public interface ConnectionPool<T extends PooledObject> extends ObjectPool<T>, StatisticListener, CleanerListener {

    @Override
    public T getObject();

    @Override
    public void freeObjects();

    @Override
    public T getNewPooledObject();

    @Override
    public String getPoolId();


}
