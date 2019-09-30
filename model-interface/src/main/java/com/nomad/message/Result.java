package com.nomad.message;

public interface Result {
    OperationStatus getOperationStatus();

    void setStatus(OperationStatus status);

    String getMessage();

    void setMessage(String message);

    String getErrorCode();

    void setErrorCode(String errorCode);

}
