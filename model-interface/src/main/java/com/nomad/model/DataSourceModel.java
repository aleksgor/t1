package com.nomad.model;

import java.util.Map;

public interface DataSourceModel {

    int getThreads();

    void setThreads(int threads);

    void addProperty(String propertyName, String propertyValue);

    String getName();

    void setName(String name);

    String getClazz();

    void setClazz(String clazz);

    Map<String, String> getProperties();

    int getTimeOut();

    void setTimeOut(int timeout);
}
