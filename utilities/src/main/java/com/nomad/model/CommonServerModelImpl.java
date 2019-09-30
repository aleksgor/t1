package com.nomad.model;

import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlElement;

import com.nomad.model.server.ProtocolType;

public class CommonServerModelImpl implements CommonServerModel {
    private String host;
    private int port;
    private int minThreads;
    private int maxThreads;
    private long keepAliveTime;
    private ProtocolType protocolType = ProtocolType.TCP;
    
    @XmlElement
    private final Map<String, String> properties = new HashMap<>();

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
        this.protocolType = protocolType;
    }

    @Override
    public Map<String, String> getProperties() {
        return properties;
    }
    
    public void addProperty(final String propertyName, final String propertyValue) {
        properties.put(propertyName, propertyValue);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((host == null) ? 0 : host.hashCode());
        result = prime * result + (int) (keepAliveTime ^ (keepAliveTime >>> 32));
        result = prime * result + maxThreads;
        result = prime * result + minThreads;
        result = prime * result + port;
        result = prime * result + ((properties == null) ? 0 : properties.hashCode());
        result = prime * result + ((protocolType == null) ? 0 : protocolType.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        CommonServerModelImpl other = (CommonServerModelImpl) obj;
        if (host == null) {
            if (other.host != null)
                return false;
        } else if (!host.equals(other.host))
            return false;
        if (keepAliveTime != other.keepAliveTime)
            return false;
        if (maxThreads != other.maxThreads)
            return false;
        if (minThreads != other.minThreads)
            return false;
        if (port != other.port)
            return false;
        if (properties == null) {
            if (other.properties != null)
                return false;
        } else if (!properties.equals(other.properties))
            return false;
        if (protocolType != other.protocolType)
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "CommonServerModelImpl [host=" + host + ", port=" + port + ", minThreads=" + minThreads + ", maxThreads=" + maxThreads + ", keepAliveTime=" + keepAliveTime
                + ", protocolType=" + protocolType + ", properties=" + properties + "]";
    }


}
