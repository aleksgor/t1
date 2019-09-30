package com.nomad.exception;

public class UnsupportedModelException extends LogicalException {

    public UnsupportedModelException() {
        super();
    }

    public UnsupportedModelException(String code, Object... args) {
        super(code, args);
    }

}
