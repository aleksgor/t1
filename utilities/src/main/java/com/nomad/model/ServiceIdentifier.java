package com.nomad.model;

import java.math.BigInteger;

public class ServiceIdentifier implements Identifier {
    private Identifier original;

    public ServiceIdentifier(Identifier original) {
        this.original = original;
    }

    @Override
    public String getModelName() {
        return original.getModelName();
    }

    public Identifier getOriginal() {
        return original;
    }

    @Override
    public void setGeneratedId(BigInteger newCode) {
        original.setGeneratedId(newCode);
    }

}
