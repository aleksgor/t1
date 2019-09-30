package com.nomad.model;

import java.util.HashMap;
import java.util.Map;

import com.nomad.model.server.ProtocolType;



public class CommonClientModelImpl implements CommonClientModel {

    protected  String host;
    protected int port;
    protected int threads;
    protected int timeout=10;
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
    public ProtocolType getProtocolType() {
        return protocolType;
    }

    @Override
    public void setProtocolType(final ProtocolType protocolType) {
        this.protocolType = protocolType;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((host == null) ? 0 : host.hashCode());
        result = prime * result + port;
        result = prime * result + ((protocolType == null) ? 0 : protocolType.hashCode());
        result = prime * result + threads;
        result = prime * result + timeout;
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final CommonClientModelImpl other = (CommonClientModelImpl) obj;
        if (host == null) {
            if (other.host != null)
                return false;
        } else if (!host.equals(other.host))
            return false;
        if (port != other.port)
            return false;
        if (protocolType != other.protocolType)
            return false;
        if (threads != other.threads)
            return false;
        if (timeout != other.timeout)
            return false;
        return true;
    }


    @Override
    public String toString() {
        return "CommonClientModelImpl [host=" + host + ", port=" + port + ", threads=" + threads + ", timeout=" + timeout + ", protocolType=" + protocolType + ", properties="
                + properties + "]";
    }

    @Override
    public Map<String, String> getProperties() {
        return properties;
    }

}
