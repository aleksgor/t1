package com.nomad.model.session;

import java.util.Properties;

public class SessionListenerModel {
    private int port;
    private int backlog;
    private int threads;
    private String host;
    private int status = 1; // 1-ok, 0-stop
    final private Properties properties = new Properties();

    public Properties getProperties() {
        return properties;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getThreads() {
        return threads;
    }

    public void setThreads(int threads) {
        this.threads = threads;
    }

    public int getBacklog() {
        return backlog;
    }

    public void setBacklog(int backlog) {
        this.backlog = backlog;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + backlog;
        result = prime * result + ((host == null) ? 0 : host.hashCode());
        result = prime * result + port;
        result = prime * result + ((properties == null) ? 0 : properties.hashCode());
        result = prime * result + status;
        result = prime * result + threads;
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
        SessionListenerModel other = (SessionListenerModel) obj;
        if (backlog != other.backlog)
            return false;
        if (host == null) {
            if (other.host != null)
                return false;
        } else if (!host.equals(other.host))
            return false;
        if (port != other.port)
            return false;
        if (properties == null) {
            if (other.properties != null)
                return false;
        } else if (!properties.equals(other.properties))
            return false;
        if (status != other.status)
            return false;
        if (threads != other.threads)
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "ListenerModel [ port=" + port + ", backlog=" + backlog + ", threads=" + threads + ", host=" + host + ", status=" + status + ", properties=" + properties + "]";
    }

}
