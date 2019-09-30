package com.nomad.communication.binders;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nomad.client.ClientPooledInterface;
import com.nomad.client.ClientStatus;
import com.nomad.exception.SystemException;
import com.nomad.message.CommonAnswer;
import com.nomad.message.CommonMessage;
import com.nomad.model.CommonClientModel;
import com.nomad.model.ConnectStatus;
import com.nomad.server.CacheServerConstants;
import com.nomad.server.ServerContext;
import com.nomad.utility.pool.ObjectPoolImpl;


public class CommonClientPool<T extends CommonMessage, K extends CommonAnswer> extends ObjectPoolImpl<ClientPooledInterface<T, K>> implements ClientStatus {

    protected static Logger LOGGER = LoggerFactory.getLogger(CommonClientPool.class);
    protected final CommonClientModel clientModel;
    protected final String clientName;
    protected final ServerContext context;
    protected ConnectStatus status;

    public CommonClientPool(final CommonClientModel clientModel, final ServerContext context, final String clientName)  {
        super(clientModel.getThreads(), clientModel.getTimeout(), 0, context, false, CacheServerConstants.Statistic.SAVE_SERVICE_GROUP_NAME);
        statisticGroupName = CacheServerConstants.Statistic.SAVE_SERVICE_GROUP_NAME;
        this.clientModel = clientModel;
        this.clientName = clientName;
        this.context = context;
    }

    @Override
    public String getPoolId() {
        return "Client:" + clientName + " host:" + clientModel.getHost() + ":" + clientModel.getPort();
    }

    @Override
    public ClientPooledInterface<T, K> getNewPooledObject() throws SystemException {
        ClientPooledInterface<T, K> connection = null;
            connection = getClient();
            LOGGER.debug(" init result:" + connection);
            connection.setPool(this);
            return connection;

    }

    protected ClientPooledInterface<T, K> getClient() throws SystemException{
        return  ServerFactory.getPooledClient(clientModel, context) ;
    }


    @Override
    protected String getInternalStatisticName() {
        String name = clientName;
        if (clientModel != null) {
            name = clientModel.getHost() + "_" + clientModel.getPort();
        }
        return name;
    }

    @Override
    public ConnectStatus getStatus() {
        return status;
    }

    @Override
    public void setStatus(final ConnectStatus status) {
        this.status = status;
    }

    public K sendMessage(final T message)throws SystemException{
        ClientPooledInterface<T, K> client=null;
        try{
            client=getClient();
            return client.sendMessage(message);
        }finally{
            if(client!=null){
                client.freeObject();
            }
        }

    }


    public int getSize(){
        return poolSize;
    }
}
