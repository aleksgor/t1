package com.nomad.server.statistic;

public class JavaVMInformationImplMBean implements JavaVMInformationMXBean {

    private long freeMemory;
    private long totalMemory;
    private long maxMemory;
    private int availableProcessors;


    @Override
    public int getAvailableProcessors() {
        return availableProcessors;
    }

    @Override
    public void setAvailableProcessors(int availableProcessors) {
        this.availableProcessors = availableProcessors;
    }

    @Override
    public long getFreeMemory() {
        return freeMemory;
    }

    @Override
    public long getTotalMemory() {
        return totalMemory;
    }

    @Override
    public long getMaxMemory() {
        return maxMemory;
    }

    @Override
    public void updateDate(long freeMemory, long totalMemory, long maxMemory) {
        this.freeMemory = freeMemory;
        this.totalMemory = totalMemory;
        this.maxMemory = maxMemory;
    }

    @Override
    public void setFreeMemory(long freeMemory) {
        this.freeMemory = freeMemory;
    }

    @Override
    public void setTotalMemory(long totalMemory) {
        this.totalMemory = totalMemory;
    }

    @Override
    public void setMaxMemory(long maxMemory) {
        this.maxMemory = maxMemory;
    }

    @Override
    public String toString() {
        return "JavaVMInformationImplMBean [freeMemory=" + freeMemory + ", totalMemory=" + totalMemory + ", maxMemory=" + maxMemory + ", availableProcessors="
                + availableProcessors + "]";
    }

}
