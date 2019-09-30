package com.nomad.server.plugin;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nomad.exception.LogicalException;
import com.nomad.exception.SystemException;
import com.nomad.model.CommandPluginModel;
import com.nomad.server.CommandPlugin;
import com.nomad.server.ServerContext;
import com.nomad.utility.pool.ObjectPoolImpl;

public class ProxyPluginClassPool extends ObjectPoolImpl<CommandPlugin> {
    private static Logger LOGGER = LoggerFactory.getLogger(ProxyPluginClassPool.class);
    private final CommandPluginModel plugin;

    private final ServerContext context;

    public ProxyPluginClassPool(final CommandPluginModel plugin, final ServerContext context) {

        super(plugin.getPoolSize(), plugin.getTimeout(),plugin.getTimeout()*2,context, false);
        this.plugin=plugin;
        this.context = context;
    }


    @Override
    public String getPoolId() {
        return plugin.getClazz();
    }

    @Override
    public CommandPlugin getNewPooledObject() throws SystemException, LogicalException {
            final CommandPlugin result = getDataInvokerInstance();
            result.init(context, plugin.getProperties());
            return result;

    }

    private CommandPlugin getDataInvokerInstance() throws SystemException {
        try {
            LOGGER.info("DataInvoker load:{}", plugin);
            final Class<?> cldi = Class.forName(plugin.getClazz());
            final CommandPlugin result = (CommandPlugin)  cldi.newInstance();
            return result;
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
            throw new SystemException("Error in search class:" + plugin.getClazz());
        }

    }

}
