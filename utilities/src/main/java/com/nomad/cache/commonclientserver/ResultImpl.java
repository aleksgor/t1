package com.nomad.cache.commonclientserver;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import com.nomad.exception.LogicalException;
import com.nomad.message.OperationStatus;
import com.nomad.message.Result;
import com.nomad.model.Identifier;
import com.nomad.utility.MessageUtil;

public class ResultImpl implements Result {

    private OperationStatus operationStatus;
    private String message;
    private String errorCode;
    private final Collection<Object> arguments= new ArrayList<>();

    public ResultImpl() {
        super();
    }

    public ResultImpl(final OperationStatus operationStatus) {
        super();
        this.operationStatus = operationStatus;
    }

    public ResultImpl(final OperationStatus operationStatus, final Collection<Identifier> ids) {
        super();
        this.arguments.addAll(ids);
        this.operationStatus = operationStatus;
    }

    public ResultImpl(LogicalException exception) {
        super();
        this.operationStatus = MessageUtil.getStatusByException(exception);
        this.arguments.addAll(Arrays.asList(exception.getArgs()));
        this.errorCode=exception.getCode();
        this.message= exception.getMessage();
        if(this.operationStatus==null){
            this.operationStatus = OperationStatus.ERROR;
        }
    }

    public ResultImpl(final OperationStatus operationStatus, final String message) {
        super();
        this.operationStatus = operationStatus;
        this.message = message;
    }

    @Override
    public OperationStatus getOperationStatus() {
        return operationStatus;
    }

    @Override
    public void setStatus(final OperationStatus status) {
        this.operationStatus = status;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public void setMessage(final String message) {
        this.message = message;
    }

    @Override
    public String getErrorCode() {
        return errorCode;
    }

    @Override
    public void setErrorCode(final String errorCode) {
        this.errorCode = errorCode;
    }


    public Collection<Object> getArguments() {
        return arguments;
    }

    @Override
    public String toString() {
        return "ResultImpl [operationStartus=" + operationStatus + ", message=" + message + ", errorCode=" + errorCode + ", arguments=" + arguments + "]";
    }


}
