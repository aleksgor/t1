package com.nomad.model;

import java.util.Properties;

public interface CommandPluginModel {

    long getCheckDelay();

    void setCheckDelay(long checkDelay);

    String getClazz();

    void setClazz(String clazz);

    int getPoolSize();

    void setPoolSize(int poolSize);

    Properties getProperties();

    int getTimeout();

    void setTimeout(int timeOut);

}
