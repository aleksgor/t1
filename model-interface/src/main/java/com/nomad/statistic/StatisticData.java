package com.nomad.statistic;

public interface StatisticData {

    void addRequest(long time, long size, long timeWait);

    void addRequest(long timeMin, long timeMax, long messageSize, long timeInUse, long timeWait, int count);

    long getMin();

    long getMax();

    int getCount();

    long getTimeWait();

    long getMessageSize();

    int getPoolSize();

    int getPoolInUse();

    void reset();

    public void setPoolSize(int poolSize);

    public void setPoolInUse(int poolInUse);

    public long getTimeInUse();

    StatisticData getCopy();
}
