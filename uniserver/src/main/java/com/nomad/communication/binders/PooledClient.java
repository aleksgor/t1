package com.nomad.communication.binders;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nomad.client.ClientInterface;
import com.nomad.client.ClientPooledInterface;
import com.nomad.exception.SystemException;
import com.nomad.message.CommonAnswer;
import com.nomad.message.CommonMessage;
import com.nomad.model.CommonClientModel;
import com.nomad.model.ConnectStatus;
import com.nomad.server.ServerContext;
import com.nomad.server.ServerContext.ServiceName;
import com.nomad.server.StoreModelService;
import com.nomad.utility.pool.PooledObjectImpl;

public  class PooledClient<K extends CommonMessage, T extends CommonAnswer> extends PooledObjectImpl implements ClientPooledInterface<K, T> {

    private static Logger LOGGER = LoggerFactory.getLogger(PooledClient.class);
    private long size = 0;
    protected final ServerContext context;
    protected static String serverName;
    protected final CommonClientModel clientModel;
    private ClientInterface<K, T> client;

    public PooledClient(final CommonClientModel clientModel, final ServerContext context, ClientInterface <K,T> client) {

        this.context = context;
        this.clientModel = clientModel;

        final StoreModelService storeModel = (StoreModelService) context.get(ServiceName.STORE_MODEL_SERVICE);
        if (storeModel != null) {
            serverName = storeModel.getServerModel().getServerName();
        } else {
            serverName = clientModel.getHost() + ":" + clientModel.getPort();
        }
        this.client=client;
    }

    @Override
    @SuppressWarnings("unchecked")
    public T sendMessage(final K message) throws SystemException {
        try {
            return client.sendMessage(message);
        } catch (final Exception e) {
            LOGGER.error("host:" + clientModel.getHost() + " port:" + clientModel.getPort() + " message:" + message + e.getMessage());
            if(pool!=null){
                ((CommonClientPool<K,T>)pool).setStatus(ConnectStatus.ERROR);
            }
            throw new SystemException(e);
        }

    }

    @Override
    protected long getSize() {
        return size;
    }

    @Override
    public void closeObject() {
        try {
            if (client != null) {
                client.close();
            }
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    @Override
    public void close() {
        closeObject();

    }

    @Override
    public long getMessageSize() {
        return client.getMessageSize();
    }

    @Override
    public void setShouldClose(boolean b) {

    }

}
