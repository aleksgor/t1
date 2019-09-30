package com.nomad.utility.pool;

public class CleanerTask implements Runnable {
    private ObjectPoolImpl<?> pool;

    public CleanerTask(ObjectPoolImpl<?> pool) {
        this.pool = pool;
    }

    @Override
    public void run() {
        pool.clean();
    }

}
