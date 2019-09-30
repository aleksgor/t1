package com.nomad.server.sessionserver;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nomad.client.ClientPooledInterface;
import com.nomad.communication.binders.CommonClientPool;
import com.nomad.communication.binders.ServerFactory;
import com.nomad.exception.SystemException;
import com.nomad.model.CommonClientModel;
import com.nomad.model.session.SessionAnswer;
import com.nomad.model.session.SessionMessage;
import com.nomad.server.CacheServerConstants;
import com.nomad.server.ServerContext;

public class SessionClientConnectionPool extends CommonClientPool<SessionMessage, SessionAnswer> {

    @SuppressWarnings("unused")
    private static Logger LOGGER = LoggerFactory.getLogger(SessionClientConnectionPool.class);
    private final ServerContext context;
    private final CommonClientModel sessionClient;


    public SessionClientConnectionPool(final CommonClientModel sessionClient, final ServerContext context)  {
        super(sessionClient, context, CacheServerConstants.Statistic.SESSION_CLIENT_GROUP_NAME);
        this.context = context;
        this.sessionClient=sessionClient;
    }

    @Override
    public String getPoolId() {

        return "CacheManagerClient:" +sessionClient.getHost()+":"+sessionClient.getPort();
    }

    @Override
    protected ClientPooledInterface<SessionMessage,SessionAnswer> getClient() throws SystemException  {
        return ServerFactory.getPooledClient(sessionClient, context);
    }


}
