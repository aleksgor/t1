package com.nomad.server;

import java.util.List;
import java.util.Vector;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nomad.exception.SystemException;

public class SchedulerServiceImpl implements SchedulerService {
    protected static Logger LOGGER = LoggerFactory.getLogger(SchedulerService.class);

    private ScheduledThreadPoolExecutor scheduler;
    private final List<ScheduledFuture<?>> futures = new Vector<>();
    private final static AtomicInteger counter = new AtomicInteger(0);
    private final String serverName;

    public SchedulerServiceImpl(final String name) {
        super();
        serverName = name;
    }

    @Override
    public void start() throws SystemException {
        scheduler = new ScheduledThreadPoolExecutor(2, new NamedThreadThreadFactory());
        scheduler.setRemoveOnCancelPolicy(true);

    }

    @Override
    public void stop() {
        synchronized (futures) {
            for (final ScheduledFuture<?> future : futures) {
                future.cancel(true);
            }
            if (scheduler != null) {
                final List<Runnable> tasks = scheduler.shutdownNow();
                for (final Runnable runnable : tasks) {
                    runnable.notifyAll();
                }
            }
            futures.clear();
        }
    }

    private ScheduledExecutorService getScheduledExecutorService() {
        if (scheduler == null) {
            scheduler = new ScheduledThreadPoolExecutor(2, new NamedThreadThreadFactory());
            scheduler.setRemoveOnCancelPolicy(true);
        }
        return scheduler;
    }

    @Override
    public ScheduledFuture<?> scheduleAtFixedRate(final Runnable runnable, final long interval, final TimeUnit timeUnit) {

        final ScheduledFuture<?> future = getScheduledExecutorService().scheduleAtFixedRate(runnable, interval, interval, timeUnit);

        futures.add(future);
        return future;
    }

    @Override
    public void stop(final ScheduledFuture<?> future) {
        future.cancel(true);
        while (!future.isCancelled()) {
            try {
                Thread.sleep(100);
            } catch (final InterruptedException e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
        futures.remove(future);
    }

    class NamedThreadThreadFactory implements ThreadFactory {

        @Override
        public Thread newThread(final Runnable r) {
            return new Thread(r, "Scheduler :" + serverName + ":" + counter.getAndIncrement());
        }
    }
}
