package com.nomad.server.statistic;

public interface ServerInformationMXBean {

    double getLoadFactor();

    void setLoadFactor(final double loadFactor);

    String getType();

    void setType(final String type);

    String getHost();

    void setHost(final String host);

    int getPort();

    void setPort(final int port);

    int getThreads();

    void setThreads(final int threads);

    int getBusy();

    void setBusy(final int busy);

    long getMinThroughPut();

    void setMinThroughPut(final long minThroughPut);

    long getMaxThroughPut();

    void setMaxThroughPut(final long maxThroughPut);

    long getAvgThroughPut();

    void setAvgThroughPut(final long avgThroughPut);

    void stop();
}
