package com.nomad.pm.exception;

import com.nomad.exception.SystemException;

public class AddModelException extends SystemException {

    public AddModelException() {
    }

    public AddModelException(String message) {
        super(message);
    }

    public AddModelException(String message, Throwable t) {
        super(message, t);
    }
}
