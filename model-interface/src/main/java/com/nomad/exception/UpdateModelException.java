package com.nomad.exception;

import com.nomad.exception.LogicalException;

public class UpdateModelException extends LogicalException {

    public UpdateModelException() {
    }

    public UpdateModelException(String code, Object... args) {
        super(code, args);
    }


}
