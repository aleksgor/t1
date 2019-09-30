package com.nomad.model;

import java.math.BigInteger;
import java.sql.Connection;
import java.util.Collection;
import java.util.Properties;

import com.nomad.exception.ModelNotExistException;
import com.nomad.exception.SystemException;
import com.nomad.model.criteria.StatisticResult;
import com.nomad.server.ServerContext;

public  interface  DataInvoker {

    void init(Properties properties, ServerContext context, String connectName) throws SystemException;

    <T extends Model> StatisticResult<T> getIds(Criteria<T> criteria) throws SystemException;

    Collection<Model> addModel(Collection<Model> models) throws SystemException;

    BigInteger getNextKey(String modelName, int count);

    Collection<Model> getModel(Collection<Identifier> identifier) throws ModelNotExistException, SystemException;

    Model getModel(Identifier identifier) throws ModelNotExistException, SystemException;

    int eraseModel(Collection<Identifier> identifiers) throws ModelNotExistException, SystemException;

    int eraseModel(Criteria<? extends Model> criteria) throws SystemException;

    Connection getConnection() throws SystemException;

    Identifier createIdentifierFromModel(Model m) throws SystemException;

    void close();

    <T extends Model> StatisticResult<T> getList(Criteria<T> criteria) throws SystemException;

    Collection<Model> updateModel(Collection<Model> input) throws SystemException;

}
