package com.nomad.server;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public interface SchedulerService extends ServiceInterface {

    ScheduledFuture<?> scheduleAtFixedRate(Runnable runnable, long interval, TimeUnit timeUnit);

    void stop(ScheduledFuture<?> future);

}
