package com.nomad.server.service.idgenerator;

import java.lang.reflect.Method;

import com.nomad.model.Identifier;
import com.nomad.server.idgenerator.AtomicBigInteger;

public class IdInformation {
    private  AtomicBigInteger counter;
    private Class<Identifier> clazz;
    private String fieldName;
    private Method method;
    private  AtomicBigInteger lastReservedCounter;


    public IdInformation() {
    }

    public IdInformation(AtomicBigInteger counter, Class<Identifier> clazz, String fieldName) {
        super();
        this.counter = counter;
        this.clazz = clazz;
        this.fieldName = fieldName;
    }

    public AtomicBigInteger getLastReservedCounter() {
        return lastReservedCounter;
    }

    public void setLastReservedCounter(AtomicBigInteger lastReservedCounter) {
        this.lastReservedCounter = lastReservedCounter;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public AtomicBigInteger getCounter() {
        return counter;
    }

    public Class<Identifier> getClazz() {
        return clazz;
    }

    public String getFieldName() {
        return fieldName;
    }

    public Method getMethod() {
        return method;
    }

    public void setCounter(AtomicBigInteger counter) {
        this.counter = counter;
    }

    public void setClazz(Class<Identifier> clazz) {
        this.clazz = clazz;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    @Override
    public String toString() {
        return "IdInformation [counter=" + counter + ", clazz=" + clazz + ", fieldName=" + fieldName + ", method=" + method + ", lastReservedCounter="
                + lastReservedCounter + "]";
    }

   
}
