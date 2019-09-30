package com.nomad.utility.pool;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nomad.exception.LogicalException;
import com.nomad.exception.SystemException;
import com.nomad.server.CleanerListener;
import com.nomad.server.ServerContext;
import com.nomad.server.ServerContext.ServiceName;
import com.nomad.server.StatisticListener;
import com.nomad.server.StoreModelService;
import com.nomad.statistic.StatisticData;
import com.nomad.utility.ObjectPool;
import com.nomad.utility.PooledObject;
import com.nomad.utility.SimpleServerContext;

public abstract class ObjectPoolImpl<T extends PooledObject> implements ObjectPool<T>, StatisticListener, CleanerListener {

    public volatile Vector<T> pool = new Vector<>();
    protected long timeout = 6000;
    private static Logger LOGGER = LoggerFactory.getLogger(ObjectPoolImpl.class);
    public volatile int poolSize = 1;
    protected volatile int maxPoolUse;
    protected volatile StatisticData collector;
    protected String statisticGroupName = null;
    private boolean dynamic = false;
    private ScheduledFuture<?> cleanScheduler;
    private ScheduledFuture<?> statisticScheduler;
    protected final ServerContext context;
    private boolean closeContext = false;

    private String serverName;

    protected ObjectPoolImpl(final int poolSize, final long timeout, final int checkDelay, final ServerContext context, final boolean dynamic) {
        this(poolSize, timeout, checkDelay, context, dynamic, null);
    }

    protected ObjectPoolImpl(final int poolSize, final long timeout, final int checkDelay, ServerContext context, final boolean dynamic, final String statisticGroupName) {
        if(poolSize==1){
            
        }
        if (context == null) {
            context = new SimpleServerContext();
            closeContext = true;
        }
        serverName = "";
        final StoreModelService storeModelService = (StoreModelService) context.get(ServiceName.STORE_MODEL_SERVICE);
        if (storeModelService != null && storeModelService.getServerModel() != null) {
            serverName = storeModelService.getServerModel().getServerName();
        }

        this.dynamic = dynamic;
        if (poolSize <= 0) {
            LOGGER.warn("Pool size is 0!" + serverName + " service:" + this.getClass().getName());
        }

        this.context = context;
        this.poolSize = poolSize;

        if (timeout == 0) {
            LOGGER.warn(" timeOut must be >0" + serverName + " serveice:" + this.getClass().getName());

        }

        this.timeout = timeout;
        if (checkDelay > 0) {
            cleanScheduler = context.getScheduledExecutorService().scheduleAtFixedRate(new CleanerTask(this), checkDelay, TimeUnit.SECONDS);
        }
    }

    public void stop() {
        if (statisticScheduler != null) {
            statisticScheduler.cancel(true);
            // context.getScheduledExecutorService(context.getServerModel()).stop(statisticScheduler);
        }
        if (cleanScheduler != null) {
            cleanScheduler.cancel(true);
            // context.getScheduledExecutorService(context.getServerModel()).stop(cleanScheduler);
        }
        if (closeContext) {
            context.close();
        }
    }

    @Override
    public void clean() {
        reapPool();

    }

    @Override
    public void collectStatistic() {
        if (collector != null) {
            if (pool.size() > 0) {
                collector.setPoolInUse(maxPoolUse);
                collector.setPoolSize(pool.size());
            }
            maxPoolUse = 0;
        }
    }

    protected String getStatisticName() {
        return statisticGroupName;
    }

    protected String getInternalStatisticName() {
        return null;
    }

    @Override
    public synchronized void init() throws SystemException, LogicalException  {
        if (dynamic) {
            final T pooledObject = getNewPooledObject();
            if (pooledObject != null) {
                pool.add(pooledObject);
            }

        } else {
            for (int i = 0; i < this.poolSize; i++) {
                final T pooledObject = getNewPooledObject();
                if (pooledObject != null) {
                    pool.add(pooledObject);
                } else {
                    return;
                }

            }
        }
    }

    @Override
    public abstract T getNewPooledObject() throws SystemException, LogicalException;

    @Override
    public abstract String getPoolId();

    protected synchronized void reapPool() {
        if (pool.size() == 0) {
            return;
        }
        final List<T> newObj = new ArrayList<>();
        final long stale = System.currentTimeMillis() - timeout;
        final Enumeration<T> objectList = pool.elements();

        while ((objectList != null) && (objectList.hasMoreElements())) {
            final T obj = objectList.nextElement();
            if ((obj.inUse()) && (stale > obj.getLastUse()) && (!obj.validate())) {
                obj.closeObject();
                removeObject(obj);
                if (!dynamic) {
                    try {
                        newObj.add(getNewPooledObject());
                    } catch (final Exception e) {

                    }
                }
            }
        }
        if (!newObj.isEmpty()) {
            pool.addAll(newObj);
        }
        if (dynamic) {
            if (pool.size() > poolSize) {
                pool.subList(0, poolSize);
            }
        }
    }

    @Override
    public synchronized void freeObjects() {

        final Enumeration<T> objectList = pool.elements();

        while ((objectList != null) && (objectList.hasMoreElements())) {
            final T obj = objectList.nextElement();
            removeObject(obj);
            obj.closeObject();
        }
    }

    private synchronized void removeObject(final T obj) {
        pool.removeElement(obj);
        obj.closeObject();
    }

    @Override
    public synchronized T getObject() {
        if (pool.size() == 0) {
            try {
                init();
            } catch (final Exception e1) {
                LOGGER.error(e1.getMessage());
                return null;
            }
        }
        T result = null;
        int tryCount = 3;
        while (result == null && tryCount >= 0) {
            for (int i = 0; i < pool.size(); i++) {
                result = pool.elementAt(i);
                if (result.lease()) {
                    if (i > maxPoolUse) { // statistic
                        maxPoolUse = i;
                    }
                    return result;
                }
            }
            try {
                Thread.sleep(50);
            } catch (final InterruptedException e) {
                LOGGER.error(" sleep error ");
            }
            tryCount--;
        }
        if (dynamic) {
            T newObject;
            try {
                newObject = getNewPooledObject();
                pool.add(newObject);
                return newObject;
            } catch (final Exception e1) {
                LOGGER.error(e1.getMessage(), e1);
            }
        }
        final Exception e = new Exception();
        LOGGER.error(" connect pool :" + pool);
        LOGGER.error(" connect pool is small size in server:" + serverName + " : " + pool.size() + " return:" + result + " class:" + this.getClass().getName() + toString(), e);
        return null;
    }

    /**
     * @param time in millis
     */
    @Override
    public final void registerRequest(final long time, final long size, final long timeWait) {
        if (collector != null) {
            collector.addRequest(time, size, timeWait);
        }
    }

    @Override
    public void close() {
        if (pool != null) {
            for (final T client : pool) {
                client.closeObject();
            }
            pool = new Vector<>();
        }
        stop();
    }

    public void addElements(final int threads) {
        if (pool == null) {
            pool = new Vector<>(threads);
        }
        poolSize += threads;
        while (pool.size() < poolSize) {
            try {
                pool.add(getNewPooledObject());
            } catch (final Exception e) {
                LOGGER.error(e.getMessage());
            }
        }
    }

    public void removeElements(int threads) {
        if (pool == null) {
            return;
        }
        poolSize -= threads;
        if (poolSize < 0) {
            poolSize = 0;
        }
        while (threads > 0 && pool.size() > 0) {
            final T forClose = getObject();
            forClose.closeObject();
            pool.remove(forClose);
            forClose.closeObject();
            threads--;
        }
    }
}