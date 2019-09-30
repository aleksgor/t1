package com.nomad.cache.commonclientserver.idgenerator;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import com.nomad.model.idgenerator.IdGeneratorCommand;
import com.nomad.model.idgenerator.IdGeneratorMessage;

public class IdGeneratorMessageImpl implements IdGeneratorMessage {

    private String modelName;
    private final List<BigInteger> value = new ArrayList<>();
    private int resultCode;
    private int count;
    private IdGeneratorCommand command;


    @Override
    public int getCount() {
        return count;
    }

    @Override
    public void setCount(int count) {
        this.count = count;
    }

    @Override
    public String getModelName() {
        return modelName;
    }

    @Override
    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    @Override
    public List<BigInteger> getValue() {
        return value;
    }


    @Override
    public int getResultCode() {
        return resultCode;
    }

    @Override
    public void setResultCode(int resultCode) {
        this.resultCode = resultCode;
    }

    @Override
    public IdGeneratorCommand getCommand() {
        return command;
    }

    @Override
    public void setCommand(IdGeneratorCommand command) {
        this.command = command;
    }

    @Override
    public String toString() {
        return "IdGeneratorMessageImpl [modelName=" + modelName + ", value=" + value + ", resultCode=" + resultCode + ", count=" + count + ", command=" + command + "]";
    }

}
