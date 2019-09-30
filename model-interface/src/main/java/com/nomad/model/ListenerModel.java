package com.nomad.model;

import java.util.Map;

import com.nomad.model.server.ProtocolType;

public interface ListenerModel {

    Map<String, String> getProperties();

    int getPort();

    void setPort(int port);

    int getBacklog();

    void setBacklog(int backlog);

    String getProtocolVersion();

    void setProtocolVersion(String protocolVersion);

    String getHost();

    void setHost(String host);

    int getStatus();

    void setStatus(int status);

    int getMinThreads();

    void setMinThreads(int minThreads);

    int getMaxThreads();

    void setMaxThreads(int maxThreads);

    ProtocolType getProtocolType();

    void setProtocolType(ProtocolType protocolType);

}
