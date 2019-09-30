package com.nomad.server.timers;

import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nomad.InternalTransactDataStore;
import com.nomad.exception.SystemException;

public class CleanerTimer extends TimerTask {
    private static Logger LOGGER = LoggerFactory.getLogger(CleanerTimer.class);
    private volatile InternalTransactDataStore store;

    public CleanerTimer(final InternalTransactDataStore store) {
        this.store = store;
    }

    @Override
    public void run() {
        final long start=System.currentTimeMillis();
        Runtime runtime = Runtime.getRuntime();
        final long freeMemory = runtime.freeMemory();
        final long maxMemory = runtime.maxMemory() / 100;
        final long percent = freeMemory / maxMemory;
        long removed=0;
        if (percent < 10) {
            LOGGER.info("clean oldobjects"+percent);
            try {
                removed=store.cleanOldData(10);
            } catch (final SystemException e) {
                LOGGER.error("error clean data", e);
            }
            runtime.gc();
            runtime = Runtime.getRuntime();

            LOGGER.info("clean finished free memory before:"+freeMemory +" free memory after:" +runtime.freeMemory()+" work time:"+ (System.currentTimeMillis()-start)+" removed:"+removed);
        }
    }

}
