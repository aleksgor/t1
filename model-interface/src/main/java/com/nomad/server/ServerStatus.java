package com.nomad.server;

public enum ServerStatus {

    OK(0), NOT_REGISTERED(1), ERROR(2), RESERVE(3), IN_SYNC(4);

    private ServerStatus(int code){
        statusCode=code;
    }
    private int statusCode;

    public int getStatusCode() {
        return statusCode;
    }

}
