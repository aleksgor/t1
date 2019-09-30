package com.nomad.server.statistic;


public interface StatisticStoreMXBean {

    String getModelName() ;

    long getCount() ;

    void clean();

    void setCount(long count);

    void setModelName(String modelName);

}
