package com.nomad.utility.pool;

public class StatisticTask implements Runnable {
    private ObjectPoolImpl<?> pool;

    public StatisticTask(ObjectPoolImpl<?> pool) {
        this.pool = pool;
    }

    @Override
    public void run() {
        pool.collectStatistic();
    }

}
