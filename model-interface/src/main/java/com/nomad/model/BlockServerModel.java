package com.nomad.model;

public interface BlockServerModel {

    int getPort();

    void setPort(int port);

    int getThreads();

    void setThreads(int threads);

    long getSessionTimeout();

    void setSessionTimeout(long sessionTimeout);

    String getHost();

    void setHost(String host);


}
