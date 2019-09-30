package com.nomad.exception;

import java.util.Arrays;

public class LogicalException extends Exception{

    String code;
    Object[] args;
    
    public LogicalException(String code) {
        super();
        this.code = code;
    }
    
    public LogicalException(String code, Object... args) {
        super();
        this.code = code;
        this.args = args;
    }
    public LogicalException() {
        super();
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Object[] getArgs() {
        return args;
    }

    public void setArgs(Object[] args) {
        this.args = args;
    }

    @Override
    public String toString() {
        return "LogicalException [code=" + code + ", args=" + Arrays.toString(args) +", message:"+getMessage()+ "]";
    }

    
}
