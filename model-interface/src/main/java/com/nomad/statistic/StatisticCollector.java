package com.nomad.statistic;

public interface StatisticCollector {

    long getMinTime();

    long getMaxTime();

    long getAvgTime();

    int getCount();

    void reset();

    long getRequestTime();

    StatisticCollector getCopy();

    long getWaitTime();

    void registerRequest(final long time, final long waitTime, long byteIn, long byteOut);

    void addStatisticCollector(final StatisticCollector collector);

    long getBytesIn();

    long getBytesOut();

}
