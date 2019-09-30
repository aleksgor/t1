package com.nomad.server.service.saveservice;

import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public class SaveSessionCleanerTimer extends TimerTask {
    private static Logger LOGGER = LoggerFactory.getLogger(SaveSessionCleanerTimer.class);
    private volatile SaveServerStore saveStore;
    private final long timeout;

    public SaveSessionCleanerTimer(final SaveServerStore saveStore, final long timeout, final String serverName) {
        this.saveStore = saveStore;
        this.timeout=timeout;
    }

    @Override
    public void run() {
        LOGGER.debug("start save leaner");
        saveStore.cleanOldSessions(timeout);

    }

}
