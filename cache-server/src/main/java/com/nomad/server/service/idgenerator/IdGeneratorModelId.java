package com.nomad.server.service.idgenerator;

import java.math.BigInteger;

import com.nomad.model.Identifier;

public class IdGeneratorModelId implements Identifier {

    public static final String MODEL_NAME = "_sys_id_generator";
    private String keyName;

    public IdGeneratorModelId(String name) {
        keyName = name;
    }
    public IdGeneratorModelId() {
    }

    @Override
    public String getModelName() {

        return MODEL_NAME;
    }

    @Override
    public void setGeneratedId(BigInteger newCode) {
        keyName = newCode.toString();
    }

    public String getKeyName() {
        return keyName;
    }

    public void setKeyName(String keyName) {
        this.keyName = keyName;
    }

}
