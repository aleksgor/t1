package com.nomad.model;


public enum RequestType {
    UNDEFINED(false), STRING(false), DOUBLE(true), DATE(false), INT(true), LONG(true), FLOAT(true), CLOB(false), BLOB(false), LIST(false), CRITERIA(false), GROUP(false);
    private boolean number;

    private RequestType(boolean number) {
        this.number = number;
    }

    public boolean isNumber() {
        return number;
    }

}
