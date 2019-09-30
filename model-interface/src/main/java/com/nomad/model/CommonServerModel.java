package com.nomad.model;

import java.util.Map;

import com.nomad.model.server.ProtocolType;


public interface CommonServerModel {

    String getHost();

    void setHost(String host);

    int getPort();

    void setPort(int port);

    int getMinThreads();

    void setMinThreads(int minThreads);

    int getMaxThreads();

    void setMaxThreads(int maxThreads);

    long getKeepAliveTime() ;

    void setKeepAliveTime(long keepAliveTime) ;

    ProtocolType getProtocolType() ;

    void setProtocolType(final ProtocolType protocolType) ;

    Map<String, String> getProperties();

}
