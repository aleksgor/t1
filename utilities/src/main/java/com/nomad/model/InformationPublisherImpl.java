package com.nomad.model;

import com.nomad.server.statistic.InformationPublisher;

public class InformationPublisherImpl implements InformationPublisher {
    private String host;
    private int port;
    private boolean security = false;
    private String passwordFile;
    private String accessFile;

    @Override
    public String getHost() {
        return host;
    }

    @Override
    public void setHost(String host) {
        this.host = host;
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
    public boolean isSecurity() {
        return security;
    }

    @Override
    public void setSecurity(boolean security) {
        this.security = security;
    }

    @Override
    public String getAccessFile() {
        return accessFile;
    }

    @Override
    public void setAccessFile(String accessFile) {
        this.accessFile = accessFile;
    }

    @Override
    public String getPasswordFile() {
        return passwordFile;
    }

    @Override
    public void setPasswordFile(String passwordFile) {
        this.passwordFile = passwordFile;
    }

    @Override
    public String toString() {
        return "InformationPublisherImpl [host=" + host + ", port=" + port + ", security=" + security + ", passwordFile=" + passwordFile + ", accessFile=" + accessFile + "]";
    }

}
