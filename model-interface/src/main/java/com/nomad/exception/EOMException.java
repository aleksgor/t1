package com.nomad.exception;

public class EOMException extends LogicalException{

    public EOMException() {
        super();
    }

    public EOMException(String code, Object... args) {
        super(code, args);
    }

    public EOMException(String code) {
        super(code);
    }

}
