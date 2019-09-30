package com.nomad.server.service.session;

import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nomad.exception.SystemException;
import com.nomad.server.SessionService;


public class SessionCleanerTimer extends TimerTask {
    private static Logger LOGGER = LoggerFactory.getLogger(SessionCleanerTimer.class);
    private volatile SessionService sessionService;
    private final String serverName;

    public SessionCleanerTimer(final SessionService sessionService, final String serverName) {
        this.sessionService = sessionService;
        this.serverName=serverName;
    }

    @Override
    public void run() {
        LOGGER.info("start session cleaner: name:" + serverName + " " + sessionService);
        try {
            sessionService.killOldSessions();
        } catch (SystemException e) {
            LOGGER.error(e.getMessage(),e);

        }

    }

}
