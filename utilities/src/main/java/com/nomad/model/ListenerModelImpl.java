package com.nomad.model;

import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.nomad.model.server.ProtocolType;

@XmlRootElement(name = "listenerModel")
@XmlAccessorType (XmlAccessType.FIELD)
public class ListenerModelImpl implements ListenerModel {
    private int port;
    private int backlog;
    private int minThreads;
    private int maxThreads;
    private String host;
    private String protocolVersion;
    private int status = 1; // 1-ok, 0-stop
    private ProtocolType protocolType=ProtocolType.TCP;

    @XmlElement(name = "properties")
    private final Map<String, String> properties = new HashMap<String, String>();

    @Override
    public Map<String, String> getProperties() {
        return  properties;
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
    public int getBacklog() {
        return backlog;
    }

    @Override
    public void setBacklog(final int backlog) {
        this.backlog = backlog;
    }

    @Override
    public String getProtocolVersion() {
        return protocolVersion;
    }

    @Override
    public void setProtocolVersion(final String protocolVersion) {
        this.protocolVersion = protocolVersion;
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
    public int getStatus() {
        return status;
    }

    @Override
    public void setStatus(final int status) {
        this.status = status;
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
        result = prime * result + backlog;
        result = prime * result + ((host == null) ? 0 : host.hashCode());
        result = prime * result + maxThreads;
        result = prime * result + minThreads;
        result = prime * result + port;
        result = prime * result + ((properties == null) ? 0 : properties.hashCode());
        result = prime * result + ((protocolType == null) ? 0 : protocolType.hashCode());
        result = prime * result + ((protocolVersion == null) ? 0 : protocolVersion.hashCode());
        result = prime * result + status;
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
        final ListenerModelImpl other = (ListenerModelImpl) obj;
        if (backlog != other.backlog)
            return false;
        if (host == null) {
            if (other.host != null)
                return false;
        } else if (!host.equals(other.host))
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
        if (protocolVersion == null) {
            if (other.protocolVersion != null)
                return false;
        } else if (!protocolVersion.equals(other.protocolVersion))
            return false;
        if (status != other.status)
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "ListenerModelImpl [ port=" + port + ", backlog=" + backlog + ", minThreads=" + minThreads + ", maxThreads=" + maxThreads + ", host=" + host
                + ", protocolVersion=" + protocolVersion + ", status=" + status + ", protocolType=" + protocolType + ", properties=" + properties + "]";
    }

}
