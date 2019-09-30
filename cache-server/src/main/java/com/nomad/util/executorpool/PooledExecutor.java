package com.nomad.util.executorpool;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.nomad.utility.pool.PooledObjectImpl;

public class PooledExecutor extends PooledObjectImpl {

    private final ThreadPoolExecutor executor;
    private long timeout=0;

    public PooledExecutor(final int size, final long timeout, final String name)  {
        this.timeout=timeout;
        executor = new ThreadPoolExecutor(size, size, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(), new ThreadPoolExecutor.CallerRunsPolicy()) {
            @Override
            protected void beforeExecute(final Thread t, final Runnable r) {
                t.setName(name);
            }

        };

    }

    public <T> List<Future<T>> executeAll(final Collection<Callable<T>> tasks)  throws InterruptedException{
        return executor.invokeAll(tasks,timeout, TimeUnit.MILLISECONDS);
    }

    public <T> T executeAny(final Collection<Callable<T>> tasks)  throws InterruptedException, ExecutionException, TimeoutException{

        return executor.invokeAny(tasks,timeout, TimeUnit.MILLISECONDS);
    }

    @Override
    public void closeObject() {
        executor.shutdownNow();
    }


    @Override
    protected long getSize() {

        return executor.getPoolSize();
    }

}
