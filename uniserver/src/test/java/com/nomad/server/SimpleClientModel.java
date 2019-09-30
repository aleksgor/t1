package com.nomad.server;

import java.util.HashMap;
import java.util.Map;

import com.nomad.model.CommonClientModel;
import com.nomad.model.server.ProtocolType;

public class SimpleClientModel implements CommonClientModel{

    private String host;
    private int port;
    private int threads;
    private int timeout;
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
    public int getThreads() {
        return threads;
    }
    @Override
    public void setThreads(final int threads) {
        this.threads = threads;
    }
    @Override
    public int getTimeout() {
        return timeout;
    }
    @Override
    public void setTimeout(final int timeout) {
        this.timeout = timeout;
    }
    @Override
    public String toString() {
        return "SimpleClientModel [host=" + host + ", port=" + port + ", threads=" + threads + ", timeout=" + timeout + "]";
    }
    @Override
    public ProtocolType getProtocolType() {
        return protocolType;
    }
    @Override
    public void setProtocolType(final ProtocolType protocolType) {
        this.protocolType = protocolType;
    }

    @Override
    public Map<String, String> getProperties() {
        return properties;
    }

}
