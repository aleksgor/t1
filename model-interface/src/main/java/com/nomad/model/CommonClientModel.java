package com.nomad.model;

import java.util.Map;

import javax.xml.bind.annotation.XmlRootElement;

import com.nomad.model.server.ProtocolType;

@XmlRootElement(name="CommonClient")

public interface CommonClientModel {

    String getHost();

    void setHost(String host);

    int getPort();

    void setPort(int port);

    int getThreads();

    void setThreads(int threads);

    int getTimeout();

    void setTimeout(int timeout);

    ProtocolType getProtocolType();

    void setProtocolType(final ProtocolType protocolType);

    Map<String, String> getProperties();

}
