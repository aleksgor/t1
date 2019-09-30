package com.nomad.server.service.session.synchronizesession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nomad.cache.commonclientserver.session.SessionMessageImpl;
import com.nomad.exception.SystemException;
import com.nomad.model.ConnectStatus;
import com.nomad.model.session.SessionAnswer;
import com.nomad.model.session.SessionClientModel;
import com.nomad.model.session.SessionCommand;
import com.nomad.model.session.SessionMessage;
import com.nomad.server.CacheServerConstants;
import com.nomad.server.ServerContext;
import com.nomad.server.service.common.NetworkConnectionPoolImpl;
import com.nomad.utility.NetworkConnectionPool;

public class SynchronizationSessionConnectPool extends NetworkConnectionPoolImpl<SessionAnswer, SessionMessage> implements NetworkConnectionPool<SessionAnswer, SessionMessage> {
    private static Logger LOGGER = LoggerFactory.getLogger(SynchronizationSessionConnectPool.class);


    public SynchronizationSessionConnectPool( final SessionClientModel client, final ServerContext context) throws SystemException {
        super(client, context,false, CacheServerConstants.Statistic.SAVE_SERVICE_GROUP_NAME);
        statisticGroupName = CacheServerConstants.Statistic.SAVE_SERVICE_GROUP_NAME;
        status=ConnectStatus.OK;
    }
    @Override
    public SynchronizeSessionClient getNewPooledObject()  {
        SynchronizeSessionClient connection = null;
        try {
            connection = new SynchronizeSessionClient(client, context);
            LOGGER.info("Init result:" + connection);
            setStatus(ConnectStatus.OK);
            connection.setPool(this);
            return connection;
        } catch (final Exception e) {
            LOGGER.error("Error create connection: "+getPoolId(), e);
            setStatus(ConnectStatus.INACCESSIBLE);
            return null;
        }
    }

    @Override
    public String getPoolId() {
        return "Synchronization Service:" + client.getHost() + ":" + client.getPort();
    }
    @Override
    public String toString() {
        return "SynchronizationSessionConnectPool [status=" + status + ", host=" + client.getHost() + ", port=" + client.getPort() + ", timeout=" + timeout + ", poolSize="
                + poolSize + ", maxPoolUse="
                + maxPoolUse + ", getPoolId()=" + getPoolId() + "]";
    }
    @Override
    public boolean connectTest() {
        SynchronizeSessionClient client=null;
        try{
            final SessionMessage message= new SessionMessageImpl();
            message.setSessionCommand(SessionCommand.TEST);
            client = (SynchronizeSessionClient) getObject();
            final SessionAnswer answer=client.sendMessage(message);
            if(answer.getResultCode()==0){
                return true;
            }
        }catch(final Exception e){

        }finally{
            if(client != null){
                client.freeObject();
            }
        }
        return false;
    }


}
