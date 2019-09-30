package com.nomad.server;

import java.util.HashMap;
import java.util.Map;

import com.nomad.model.CommonServerModel;
import com.nomad.model.server.ProtocolType;

public class SimpleServerModel implements CommonServerModel {
    private String host;
    private int port;
    private int minThreads;
    private int maxThreads;
    private long keepAliveTime;
    protected ProtocolType protocolType = ProtocolType.TCP;
    protected final Map<String, String> properties = new HashMap<>();

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
    public int getMinThreads() {
        return minThreads;
    }

    @Override
    public void setMinThreads(final int minThreads) {
        this.minThreads = minThreads;
    }

    @Override
    public int getMaxThreads() {
        return maxThreads;
    }

    @Override
    public void setMaxThreads(final int maxThreads) {
        this.maxThreads = maxThreads;
    }

    @Override
    public long getKeepAliveTime() {
        return keepAliveTime;
    }

    @Override
    public void setKeepAliveTime(final long keepAliveTime) {
        this.keepAliveTime = keepAliveTime;
    }

    @Override
    public ProtocolType getProtocolType() {
        return protocolType;
    }

    @Override
    public void setProtocolType(final ProtocolType protocolType) {
        this.protocolType=protocolType;
    }

    @Override
    public Map<String, String> getProperties() {
        return properties;
    }

}
