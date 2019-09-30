package com.nomad.util;

import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nomad.exception.LogicalException;
import com.nomad.exception.SystemException;
import com.nomad.model.DataInvoker;
import com.nomad.server.ServerContext;
import com.nomad.utility.DataInvokerPool;
import com.nomad.utility.PooledDataInvoker;
import com.nomad.utility.pool.ObjectPoolImpl;

public class DataInvokerPoolImpl extends ObjectPoolImpl<PooledDataInvoker> implements DataInvokerPool {

    private static Logger LOGGER = LoggerFactory.getLogger(DataInvokerPoolImpl.class);
    private final String className;
    private final  Map<String,String> properties;
    private final ServerContext context;
    private final String connectName;

    public DataInvokerPoolImpl(final int threads, final int timeout, final String className, final Map<String, String> properties, final ServerContext context, String connectName)
            throws SystemException {
        super(threads, timeout, timeout*2, context, false);
        this.className = className;
        this.properties = properties;
        this.context=context;
        this.connectName = connectName;
        LOGGER.info("datainvokerPool init:{}", threads);
    }

    @Override
    public String getPoolId() {

        return this.getClass().getName()+":"+className;
    }

    private DataInvoker getDataInvokerInstance() throws SystemException {
        try {

            LOGGER.info("DataInvoker load:{}", className);
            final Class<?> clazz = Class.forName(className);
            final DataInvoker dataInvoker = (DataInvoker) clazz.newInstance();
            return dataInvoker;
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
            throw new SystemException("Cannot found class:" + className);
        }

    }

    @Override
    public synchronized PooledDataInvoker getObject() {
        PooledDataInvoker result = super.getObject();
        return result;
    }

    @Override
    public PooledDataInvoker getNewPooledObject() throws SystemException, LogicalException {
        final DataInvoker dataInvoker = getDataInvokerInstance();
        final Properties properties=new Properties();
        if (properties != null) {
            properties.putAll(this.properties);
        }
        dataInvoker.init(properties, context, connectName);
        return new PooledDataInvokerImpl(dataInvoker);
    }

    @Override
    public void incrementPoolSize(final int addSize) {
        poolSize += addSize;
    }
}
