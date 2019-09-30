package com.nomad.statistic;

public interface PoolInfo {

    int getSize();

    int getInUse();

    void setSize(int size);

    void setInUse(int inUse);

}
