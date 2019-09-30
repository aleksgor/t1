package com.nomad.model.idgenerator;

import java.math.BigInteger;
import java.util.List;

import com.nomad.message.CommonAnswer;
import com.nomad.message.CommonMessage;

public interface IdGeneratorMessage extends CommonMessage, CommonAnswer {

    String getModelName();

    void setModelName(final String modelName);

    List<BigInteger> getValue();

    void setResultCode(int resultCode);

    void setCommand(IdGeneratorCommand command);

    IdGeneratorCommand getCommand();

    int getCount();

    void setCount(int count);

}
