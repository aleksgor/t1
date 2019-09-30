package com.nomad.cache.test.userdataaccess;

import java.math.BigInteger;
import java.sql.Connection;
import java.util.Collection;
import java.util.Properties;

import com.nomad.exception.ModelNotExistException;
import com.nomad.exception.SystemException;
import com.nomad.model.Criteria;
import com.nomad.model.DataInvoker;
import com.nomad.model.Identifier;
import com.nomad.model.Model;
import com.nomad.model.criteria.StatisticResult;
import com.nomad.pm.PmDataInvokerFactory;
import com.nomad.server.PmDataInvoker;
import com.nomad.server.ServerContext;

public class BaseDataInvoker implements DataInvoker {

    private PmDataInvoker dataInvoker;
    @SuppressWarnings("unused")
    private ServerContext context;
    @Override
    public void init(Properties properties, ServerContext context, String connectName) throws SystemException {
        this.context = context;
        dataInvoker =context.getPmDataInvoker(connectName);
        if(dataInvoker==null){
            String driver = properties.getProperty("driver");
            String url = properties.getProperty("url");
            String user = properties.getProperty("user");
            String password = properties.getProperty("password");
            String configurationFile = properties.getProperty("fileConfiguration");
            int threads = Integer.parseInt(properties.getProperty("threads", "10"));
            dataInvoker=PmDataInvokerFactory.getDataInvoker(connectName, driver, url, user, password, configurationFile, threads, context);
            context.putPmDataInvoker(connectName, dataInvoker);
        }
    }

    @Override
    public <T extends Model> StatisticResult<T> getList(Criteria<T> criteria) throws SystemException {

        return dataInvoker.getList(criteria);
    }


    @Override
    public BigInteger getNextKey(String tableName, int count) {

        return dataInvoker.getNextKey(tableName, count);
    }

    @Override
    public Model getModel(Identifier identifier) throws ModelNotExistException, SystemException {

        return dataInvoker.getModel(identifier);
    }


    @Override
    public int eraseModel(Criteria<? extends Model> criteria) throws SystemException {

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
    public Collection<Model> addModel(Collection<Model> models) throws SystemException {
        return dataInvoker.addModel(models);
    }

    @Override
    public Collection<Model> updateModel(Collection<Model> m) throws SystemException {
        return dataInvoker.updateModel(m);
    }

    @Override
    public Collection<Model> getModel(Collection<Identifier> identifier) throws ModelNotExistException, SystemException {
        return dataInvoker.getModel(identifier);
    }

    @Override
    public int eraseModel(Collection<Identifier> identifier) throws ModelNotExistException, SystemException {
        return dataInvoker.eraseModel(identifier);
    }

    @Override
    public <T extends Model> StatisticResult<T> getIds(Criteria<T> criteria) throws SystemException {
        return dataInvoker.getIds(criteria);
    }
}
