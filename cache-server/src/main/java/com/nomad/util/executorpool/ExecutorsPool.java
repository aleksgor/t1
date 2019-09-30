package com.nomad.util.executorpool;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nomad.server.CacheServerConstants;
import com.nomad.server.ServerContext;
import com.nomad.utility.pool.ObjectPoolImpl;

public class ExecutorsPool extends ObjectPoolImpl<PooledExecutor> {

    private static Logger LOGGER = LoggerFactory.getLogger(ExecutorsPool.class);
    private final String poolName;

    public ExecutorsPool(final int threads, final ServerContext context, final String name, final long timeout) {

        super(threads, timeout, 2000, context, false, CacheServerConstants.Statistic.CACHE_MANAGER_CLIENT_GROUP_NAME);
        poolSize = threads;
        poolName = name;
    }

    @Override
    public String getPoolId() {
        return poolName;
    }

    @Override
    public PooledExecutor getNewPooledObject() {
        final PooledExecutor executor = new PooledExecutor(poolSize, timeout, poolName);
        return executor;
    }

    public void setNewSize(final int newSize) {
        LOGGER.info("set new size of Executors old value:" + poolSize + " new value:" + newSize);
        if (poolSize == newSize) {
            return;
        }
        poolSize = newSize;
        if (poolSize < newSize) {
            while (pool.size() < newSize) {
                pool.add(getNewPooledObject());
            }
        }
    }

    @Override
    public void close() {
        super.close();
    }


}
