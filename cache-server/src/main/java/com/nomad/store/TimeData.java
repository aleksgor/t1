package com.nomad.store;

public class TimeData<T> {
    private volatile T data;
    private volatile long time;

    public TimeData(final T data) {
        super();
        this.data = data;
        time = System.currentTimeMillis();
    }

    public T getData() {
        time = System.currentTimeMillis();
        return data;
    }

    public long getTime() {
        return time;
    }

}
