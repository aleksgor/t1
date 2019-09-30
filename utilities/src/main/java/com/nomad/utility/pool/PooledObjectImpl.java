package com.nomad.utility.pool;

import com.nomad.utility.ObjectPool;
import com.nomad.utility.PooledObject;

public abstract class PooledObjectImpl implements PooledObject {

    protected volatile boolean inUse;
    protected long timeStamp;
    protected volatile ObjectPool<? extends PooledObject> pool;
    protected long timeWait;

    @Override
    public void setPool(ObjectPool<? extends PooledObject> pool) {
        this.pool = pool;
    }

    public PooledObjectImpl() {
        this.inUse = false;
        this.timeStamp = System.currentTimeMillis();
        ;
        this.timeWait = 0;
    }

    @Override
    public synchronized boolean lease() {
        if (inUse) {
            return false;
        } else {
            inUse = true;
            return true;
        }
    }

    @Override
    public boolean validate() {
        return true;
    }

    @Override
    public boolean inUse() {
        return inUse;
    }

    @Override
    public long getLastUse() {
        return timeStamp;
    }

    @Override
    public void freeObject() {
        if (pool != null) {
            long timeNow = System.currentTimeMillis();
            pool.registerRequest(timeNow - timeStamp, getSize(), timeWait);
            timeStamp = timeNow;
        }
        inUse = false;
    }

    @Override
    public abstract void closeObject();

    protected abstract long getSize();

    @Override
    public String toString() {
        return "Pooled Object impl [inUse=" + inUse + ", timestamp=" + timeStamp + ", pool=" + pool + "]";
    }

}
