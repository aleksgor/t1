package com.nomad.model.idgenerator;

import java.math.BigInteger;
import java.util.List;

import com.nomad.exception.LogicalException;
import com.nomad.exception.SystemException;
import com.nomad.model.Identifier;
import com.nomad.server.ServiceInterface;

public interface IdGeneratorService extends ServiceInterface {

    public List<BigInteger> nextId(String modelName, int count);

    public List<Identifier> nextIdentifier(String modelName, int count) throws LogicalException, SystemException;


}
