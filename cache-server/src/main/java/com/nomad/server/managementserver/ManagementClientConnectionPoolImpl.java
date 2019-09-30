package com.nomad.server.managementserver;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nomad.client.ClientPooledInterface;
import com.nomad.communication.binders.CommonClientPool;
import com.nomad.communication.binders.ServerFactory;
import com.nomad.exception.SystemException;
import com.nomad.message.ManagementMessage;
import com.nomad.model.management.ManagementClientModel;
import com.nomad.server.CacheServerConstants;
import com.nomad.server.ServerContext;
import com.nomad.server.service.ManagementClientConnectionPool;

public class ManagementClientConnectionPoolImpl extends CommonClientPool<ManagementMessage, ManagementMessage> implements ManagementClientConnectionPool {

    @SuppressWarnings("unused")
    private static Logger LOGGER = LoggerFactory.getLogger(ManagementClientConnectionPoolImpl.class);
    private final ServerContext context;
    private final ManagementClientModel managemnetClient;

    public ManagementClientConnectionPoolImpl(final ManagementClientModel managemnetClient, final ServerContext context)  {
        super(managemnetClient, context, CacheServerConstants.Statistic.MANAGEMENT_CLIENT_GROUP_NAME);
        this.context = context;
        this.managemnetClient=managemnetClient;
    }

    @Override
    public String getPoolId() {

        return "ManagementClientConnectionPool:" +managemnetClient.getHost()+":"+managemnetClient.getPort();
    }

    @Override
    public ClientPooledInterface<ManagementMessage, ManagementMessage> getClient() throws SystemException {
        return ServerFactory.getPooledClient(managemnetClient, context);
    }

    @Override
    public void start() throws SystemException {

    }


}
