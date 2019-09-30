package com.nomad.utility;


public interface DataInvokerPool{

    String getPoolId();

    void incrementPoolSize(int newSize);

    PooledDataInvoker getObject();

    void close();
}
