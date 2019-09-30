package com.nomad.exception;

import com.nomad.exception.LogicalException;

public class RelationNotExistException extends LogicalException {

    public RelationNotExistException() {
    }

    public RelationNotExistException(String code, Object... args) {
        super(code, args);
    }

    public RelationNotExistException(String code) {
        super(code);
    }

}
