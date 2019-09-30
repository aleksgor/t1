package com.nomad.server;

public enum SessionResult {
    OK(0),ERROR(-1),ACCESS_DENIED(-2),OPERATION_DENIED(-3),TIME_OUT(-4),NO_SESSION(-5);

    private int code;

    private SessionResult(final int code){
        this.code=code;
    }

    public int getCode() {
        return code;
    }

    public void setCode(final char code) {
        this.code = code;
    }


}
