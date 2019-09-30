package com.nomad.exception;

import com.nomad.exception.LogicalException;

public class ModelNotExistException extends LogicalException {

    public ModelNotExistException() {
    }

    public ModelNotExistException(String code, Object... args) {
        super(code, args);
    }

    public ModelNotExistException(String code) {
        super(code);
    }

}
