package com.nomad.server;

import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nomad.exception.LogicalException;
import com.nomad.exception.SystemException;
import com.nomad.model.CommandPluginModelImpl;
import com.nomad.utility.pool.ObjectPoolImpl;

public class PluginClassPool extends ObjectPoolImpl<CommonCommandPlugin> {
    private static Logger LOGGER = LoggerFactory.getLogger(PluginClassPool.class);
    private final Properties properties;
    private final String className;
    private final ServerContext context;

    public PluginClassPool(final CommandPluginModelImpl plugin,  final ServerContext context) {
        super(plugin.getPoolSize(), plugin.getTimeout(),plugin.getTimeout()*2,context, false);
        className = plugin.getClazz();
        properties=plugin.getProperties();
        this.context=context;
    }
    @Override
    public String getPoolId() {

        return this.getClass().getName()+":"+className;
    }
    @Override
    public CommonCommandPlugin getNewPooledObject() throws SystemException, LogicalException {
        try {
            final CommonCommandPlugin result = getDataInvokerInstance();
            result.init(context,properties);
            return result;
        } catch (final SystemException e) {
            LOGGER.error(e.getMessage(), e);
        }

        return null;
    }

    private CommonCommandPlugin getDataInvokerInstance() throws SystemException {
        try {
            LOGGER.info("DataInvoker load:{}", className);
            final Class<?> cldi = Class.forName(className);
            final CommonCommandPlugin result = (CommonCommandPlugin) cldi.newInstance();
            return result;
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
            throw new SystemException("error in search class:" + className);
        }

    }



}
