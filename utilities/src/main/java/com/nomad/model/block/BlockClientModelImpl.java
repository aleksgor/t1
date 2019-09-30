package com.nomad.model.block;

import com.nomad.model.BlockClientModel;

public class BlockClientModelImpl implements BlockClientModel {

    private int port;
    private int threads;
    private String host;
    private int timeOut;

    @Override
    public int getTimeOut() {
        return timeOut;
    }

    @Override
    public void setTimeOut(int timeOut) {
        this.timeOut = timeOut;
    }

    @Override
    public int getPort() {
        return port;
    }

    @Override
    public void setPort(int port) {
        this.port = port;
    }

    @Override
    public int getThreads() {
        return threads;
    }

    @Override
    public void setThreads(int threads) {
        this.threads = threads;
    }

    @Override
    public String getHost() {
        return host;
    }

    @Override
    public void setHost(String host) {
        this.host = host;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((host == null) ? 0 : host.hashCode());
        result = prime * result + port;
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
        BlockClientModelImpl other = (BlockClientModelImpl) obj;
        if (host == null) {
            if (other.host != null)
                return false;
        } else if (!host.equals(other.host))
            return false;
        if (port != other.port)
            return false;
        if (threads != other.threads)
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "BlockServerModelImpl [port=" + port + ", threads=" + threads + ", host=" + host + "]";
    }

}
