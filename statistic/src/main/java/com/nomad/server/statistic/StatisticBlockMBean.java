package com.nomad.server.statistic;

import com.nomad.server.ServiceInterface;

public class StatisticBlockMBean implements StatisticBlockMXBean {
    private int blockCount;
    private int SessionCount;
    private ServiceInterface serviceInterface;

    @Override
    public int getBlockCount() {
        return blockCount;
    }

    @Override
    public void setBlockCount(int blockCount) {
        this.blockCount = blockCount;
    }

    @Override
    public int getSessionCount() {
        return SessionCount;
    }

    @Override
    public void setSessionCount(int sessionCount) {
        SessionCount = sessionCount;
    }

    @Override
    public void stop() {
        serviceInterface.stop();
    }

    public void setServiceInterface(ServiceInterface serviceInterface) {
        this.serviceInterface = serviceInterface;
    }

}
