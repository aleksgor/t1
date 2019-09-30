package com.nomad.server.statistic;

public interface StatisticBlockMXBean {
    int getBlockCount();

    int getSessionCount();

    void stop();

    void setSessionCount(int sessionCount);

    void setBlockCount(int blockCount);
}
