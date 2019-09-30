package com.nomad.server.statistic.impl;

import com.nomad.statistic.StatisticCollector;

public class StatisticCollectorImpl implements StatisticCollector {
    private long minTime = 0;
    private long maxTime = 0;
    private long fullTime = 0;
    private int count = 0;
    private boolean empty = true;
    private long waitTime = 0;
    private long bytesIn;
    private long bytesOut;

    public StatisticCollectorImpl() {
        super();
    }

    public StatisticCollectorImpl(final long minTime, final long maxTime, final long fullTime, final int count, final long waitTime) {
        super();
        this.minTime = minTime;
        this.maxTime = maxTime;
        this.fullTime = fullTime;
        this.count = count;
        this.waitTime = waitTime;
    }

    @Override
    public void registerRequest(final long time, final long waitTime, final long byteIn, final long byteOut) {
        if (empty) {
            fullTime = minTime = maxTime = time;
            count = 1;
            empty = false;
            return;
        }

        if (minTime > time) {
            minTime = time;
        }
        if (maxTime < time) {
            maxTime = time;
        }
        this.waitTime += waitTime;
        bytesIn += byteIn;
        bytesOut += byteOut;
        fullTime += time;
        count++;
    }

    @Override
    public long getBytesIn() {
        return bytesIn;
    }

    @Override
    public long getBytesOut() {
        return bytesOut;
    }

    @Override
    public long getMinTime() {
        return minTime;
    }

    @Override
    public long getMaxTime() {
        return maxTime;
    }

    @Override
    public long getAvgTime() {
        return (long) ((fullTime) / (double) count);
    }

    @Override
    public int getCount() {
        return count;
    }

    @Override
    public long getRequestTime() {
        return fullTime;
    }

    @Override
    public long getWaitTime() {
        return waitTime;
    }

    @Override
    public void reset() {
        minTime = 0;
        maxTime = 0;
        fullTime = 0;
        count = 0;
        waitTime = 0;
        empty = true;

    }

    @Override
    public void addStatisticCollector(final StatisticCollector collector) {
        if (empty) {
            fullTime = collector.getRequestTime();
            minTime = collector.getMinTime();
            maxTime = collector.getMaxTime();
            count = collector.getCount();
            waitTime = collector.getWaitTime();
            empty = false;
            return;
        }

    }

    @Override
    public StatisticCollector getCopy() {
        final StatisticCollector result = new StatisticCollectorImpl(minTime, maxTime, fullTime, count, waitTime);
        reset();
        return result;
    }

    @Override
    public String toString() {
        return "StatisticCollectorImpl [minTime=" + minTime + ", maxTime=" + maxTime + ", fullTime=" + fullTime + ", count=" + count + ", empty=" + empty
                + ", waitTime=" + waitTime + ", bytesIn=" + bytesIn + ", bytesOut=" + bytesOut + "]";
    }

}
