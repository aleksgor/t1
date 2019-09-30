package com.nomad.server.statistic;

import com.nomad.server.ServerListener;
import com.nomad.server.Status;

public class ServerInformationImplMBean implements ServerInformationMXBean {

    private String host;
    private String type;
    private int port;
    private int threads = 0;
    private volatile Integer busy = 0;
    private Status status;
    private long minThroughPut;
    private long maxThroughPut;
    private long avgThroughPut;
    private double loadFactor;

    private ServerListener listener;

    @Override
    public void stop(){
        listener.stop();
    }
    public void setListener(final ServerListener listener) {
        this.listener = listener;
    }

    @Override
    public double getLoadFactor() {
        return loadFactor;
    }

    @Override
    public void setLoadFactor(final double loadFactor) {
        this.loadFactor = loadFactor;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public void setType(final String type) {
        this.type = type;
    }

    @Override
    public String getHost() {
        return host;
    }

    @Override
    public void setHost(final String host) {
        this.host = host;
    }

    @Override
    public int getPort() {
        return port;
    }

    @Override
    public void setPort(final int port) {
        this.port = port;
    }

    @Override
    public int getThreads() {
        return threads;
    }

    @Override
    public void setThreads(final int threads) {
        this.threads = threads;
    }

    @Override
    public int getBusy() {
        return busy;
    }

    @Override
    public void setBusy(final int busy) {
        synchronized (this.busy) {
            this.busy = busy;

        }
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(final Status status) {
        this.status = status;
    }

    @Override
    public long getMinThroughPut() {
        return minThroughPut;
    }

    @Override
    public void setMinThroughPut(final long minThroughPut) {
        this.minThroughPut = minThroughPut;
    }

    @Override
    public long getMaxThroughPut() {
        return maxThroughPut;
    }

    @Override
    public void setMaxThroughPut(final long maxThroughPut) {
        this.maxThroughPut = maxThroughPut;
    }

    @Override
    public long getAvgThroughPut() {
        return avgThroughPut;
    }

    @Override
    public void setAvgThroughPut(final long avgThroughPut) {
        this.avgThroughPut = avgThroughPut;
    }

    @Override
    public String toString() {
        return "ServerInformationImplMBean [host=" + host + ", type=" + type + ", port=" + port + ", threads=" + threads + ", busy=" + busy + ", status="
 + status
                + ", minThroughPut=" + minThroughPut + ", maxThroughPut=" + maxThroughPut + ", avgThroughPut=" + avgThroughPut + ", loadFactor="
                + loadFactor + "]";
    }

}
