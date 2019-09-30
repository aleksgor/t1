package com.nomad.server.statistic;

public interface JavaVMInformationMXBean {

    long getFreeMemory();

    long getTotalMemory();

    long getMaxMemory();

    int getAvailableProcessors();

    void setFreeMemory(long freeMemory);

    void setTotalMemory(long totalMemory);

    void setMaxMemory(long maxMemory);

    void setAvailableProcessors(int availableProcessors);

    void updateDate(long freeMemory, long totalMemory, long maxMemory);

}
