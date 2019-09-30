package com.nomad.utility;

public interface PooledObject {

    boolean lease();

    boolean validate();

    boolean inUse();

    long getLastUse();

    void freeObject();

    void closeObject();

    void setPool(ObjectPool<? extends PooledObject> pool);
}
