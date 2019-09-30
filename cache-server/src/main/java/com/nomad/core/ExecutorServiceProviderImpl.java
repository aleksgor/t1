package com.nomad.core;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nomad.exception.SystemException;
import com.nomad.server.ExecutorServiceProvider;

public class ExecutorServiceProviderImpl implements ExecutorServiceProvider {
    protected static Logger LOGGER = LoggerFactory.getLogger(ExecutorServiceProvider.class);
    private ThreadPoolExecutor executor;

    public ExecutorServiceProviderImpl(final String name, int count) {
        executor = new ThreadPoolExecutor(count / 3, count, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(), new ThreadPoolExecutor.CallerRunsPolicy()) {
            @Override
            protected void beforeExecute(final Thread t, final Runnable r) {
                t.setName("proxy executor :" + name);
            }
        };
    }

    @Override
    public ExecutorService getExecutorService() {
        return executor;
    }

    @Override
    public void start() throws SystemException {

    }

    @Override
    public void stop() {
        if (executor != null) {
            executor.shutdownNow();
            while(!executor.isShutdown()){
                try {
                    Thread.sleep(100);
                } catch (final InterruptedException e) {
                    LOGGER.error(e.getMessage(), e);
                }
            }
        }

    }
}
