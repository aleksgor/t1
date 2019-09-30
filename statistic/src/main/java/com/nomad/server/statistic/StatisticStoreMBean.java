package com.nomad.server.statistic;

import com.nomad.server.ModelStore;

public class StatisticStoreMBean implements StatisticStoreMXBean {

    private String modelName;
    private long count;
    private ModelStore<?> modelStore;

    @Override
    public String getModelName() {
        return modelName;
    }

    @Override
    public long getCount() {
        return count;
    }

    @Override
    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    @Override
    public void setCount(long count) {
        this.count = count;
    }

    @Override
    public void clean() {
        modelStore.clean();

    }

    public void setModelStore(ModelStore<?> modelStore) {
        this.modelStore = modelStore;
    }

}
