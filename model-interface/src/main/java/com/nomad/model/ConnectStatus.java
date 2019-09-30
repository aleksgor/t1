package com.nomad.model;

public enum ConnectStatus {
    UNKNOWN(-1), OK(0), INACCESSIBLE(-2), ERROR(-3);
    private final int code;
    private ConnectStatus(final int code){
        this.code=code;
    }
    public int getCode() {
        return code;
    }

}
