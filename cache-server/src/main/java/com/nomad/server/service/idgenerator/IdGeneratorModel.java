package com.nomad.server.service.idgenerator;

import java.math.BigInteger;

import com.nomad.model.Identifier;
import com.nomad.model.Model;

public class IdGeneratorModel implements Model {

    private IdGeneratorModelId identifier;
    private String keyName;
    private BigInteger value;

    @Override
    public Identifier getIdentifier() {

        return identifier;
    }

    @Override
    public void setIdentifier(Identifier id) {
        identifier = (IdGeneratorModelId) id;
        keyName = identifier.getKeyName();

    }

    @Override
    public String getModelName() {

        return IdGeneratorModelId.MODEL_NAME;
    }

    public String getKeyName() {
        return keyName;
    }

    public void setKeyName(String keyName) {
        this.keyName = keyName;
    }

    public BigInteger getValue() {
        return value;
    }

    public void setValue(BigInteger value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "IdGeneratorModel [identifier=" + identifier + ", keyName=" + keyName + ", value=" + value + "]";
    }

}
