package com.nomad.util;

import java.math.BigInteger;
import java.sql.Connection;
import java.util.Collection;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nomad.exception.ModelNotExistException;
import com.nomad.exception.SystemException;
import com.nomad.model.Criteria;
import com.nomad.model.DataInvoker;
import com.nomad.model.Identifier;
import com.nomad.model.Model;
import com.nomad.model.criteria.StatisticResult;
import com.nomad.server.ServerContext;
import com.nomad.utility.PooledDataInvoker;
import com.nomad.utility.pool.PooledObjectImpl;

public class PooledDataInvokerImpl extends PooledObjectImpl implements PooledDataInvoker {

    private DataInvoker dataInvoker;
    private static Logger LOGGER = LoggerFactory.getLogger(PooledDataInvokerImpl.class);


    public PooledDataInvokerImpl(DataInvoker dataInvoker) {
        this.dataInvoker = dataInvoker;
    }

    @Override
    public void closeObject() {

    }


    @Override
    public void init(Properties properties, ServerContext context, String name) throws SystemException {
        dataInvoker.init(properties, context, name);

    }

    @Override
    public <T extends Model>StatisticResult<T> getList(Criteria<T> criteria) throws SystemException {
        return dataInvoker.getList(criteria);
    }

    @Override
    public Collection<Model> addModel(Collection<Model> m) throws SystemException {
        LOGGER.debug("addModel:{}",m);
        return dataInvoker.addModel(m);
    }

    @Override
    public Collection<Model> updateModel(Collection<Model> m) throws SystemException {
        return dataInvoker.updateModel(m);
    }

    @Override
    public BigInteger getNextKey(String tableName, int count) {

        return dataInvoker.getNextKey(tableName, count);
    }

    @Override
    public Collection<Model> getModel(Collection<Identifier> identifier) throws ModelNotExistException, SystemException {

        return dataInvoker.getModel(identifier);
    }

    @Override
    public int eraseModel(Collection<Identifier> identifiers) throws ModelNotExistException, SystemException {
        LOGGER.debug("eraseModel:{}", identifiers);
        return dataInvoker.eraseModel(identifiers);
    }

    @Override
    public int eraseModel(Criteria<? extends Model> criteria) throws SystemException {
        LOGGER.debug("eraseModel:{}", criteria);
        return dataInvoker.eraseModel(criteria);
    }

    @Override
    public Connection getConnection() throws SystemException {
        return dataInvoker.getConnection();
    }

    @Override
    public Identifier createIdentifierFromModel(Model m) throws SystemException {
        return dataInvoker.createIdentifierFromModel(m);
    }

    @Override
    public void close() {
        dataInvoker.close();
    }



    @Override
    public Model getModel(Identifier identifier) throws ModelNotExistException, SystemException {
        return dataInvoker.getModel(identifier);
    }

  
    @Override
    protected long getSize() {
        return 0;
    }

    @Override
    public <T extends Model> StatisticResult<T> getIds(Criteria<T> criteria) throws SystemException {
        return dataInvoker.getIds(criteria);
    }

}
