package com.nomad.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nomad.model.ServerModelImpl;

public class ThreadServerLauncher {
    protected static Logger LOGGER = LoggerFactory.getLogger(ThreadServerLauncher.class);

    public static final int Started=1;
    public static final int Stoped=0;
    private final ServerLauncher launcher;

    private volatile int status=Stoped;

    public ThreadServerLauncher(final ServerLauncher launcher) {
        super();
        this.launcher = launcher;
    }

    public ThreadServerLauncher(final ServerModelImpl server) {
        super();
        launcher = new ServerLauncher(server);
    }

    public void start() throws InterruptedException {
        final Thread t = new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    launcher.start();
                    status=Started;
                } catch (final Exception e) {
                    LOGGER.error(e.getMessage(), e);
                }
            }

        });
        t.start();
        while(status!=Started){
            Thread.sleep(1000);
        }
    }

    public int getStatus() {
        return status;
    }

    public void stop() {
        launcher.stop();
        status=Stoped;

    }
}
