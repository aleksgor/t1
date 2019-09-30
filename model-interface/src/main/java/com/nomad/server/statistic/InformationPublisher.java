package com.nomad.server.statistic;

public interface InformationPublisher {
    String getHost();

    void setHost(String host);

    int getPort();

    void setPort(int port);

    boolean isSecurity();

    void setSecurity(boolean security);

    String getAccessFile();

    void setAccessFile(String accessFile);

    String getPasswordFile();

    void setPasswordFile(String passwordFile);

}
