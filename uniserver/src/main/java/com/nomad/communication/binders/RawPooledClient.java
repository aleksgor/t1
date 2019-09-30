package com.nomad.communication.binders;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nomad.client.ClientStatus;
import com.nomad.client.RawClientInterface;
import com.nomad.client.RawClientPooledInterface;
import com.nomad.exception.SystemException;
import com.nomad.message.RawMessage;
import com.nomad.model.CommonClientModel;
import com.nomad.model.ConnectStatus;
import com.nomad.server.ServerContext;
import com.nomad.server.ServerContext.ServiceName;
import com.nomad.server.StoreModelService;
import com.nomad.server.processing.ObjectProcessing;
import com.nomad.utility.pool.PooledObjectImpl;

public  class RawPooledClient extends PooledObjectImpl implements RawClientPooledInterface {

    private static Logger LOGGER = LoggerFactory.getLogger(PooledClient.class);
    private long size = 0;
    protected final ServerContext context;
    protected static String serverName;
    protected final CommonClientModel clientModel;
    private RawClientInterface client;

    public RawPooledClient(final CommonClientModel clientModel, final ServerContext context, RawClientInterface client) {

        this.context = context;
        this.clientModel = clientModel;

        final StoreModelService storeModelService = (StoreModelService) context.get(ServiceName.STORE_MODEL_SERVICE);
        if (storeModelService != null) {
            serverName = storeModelService.getServerModel().getServerName();
        } else {
            serverName = clientModel.getHost() + ":" + clientModel.getPort();
        }
        this.client=client;
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
    public RawMessage sendRawMessage(RawMessage message) throws SystemException {
        try {
            return client.sendRawMessage(message);
        } catch (final Exception e) {
            LOGGER.error("host:" + clientModel.getHost() + " port:" + clientModel.getPort() + " message:" + message + e.getMessage());
            if(pool!=null){
                ((ClientStatus) pool).setStatus(ConnectStatus.ERROR);
            }
            throw e;
        }
    }

    @Override
    public long getMessageSize() {
        return client.getMessageSize();
    }

    @Override
    public void setShouldClose(boolean b) {

    }


    @Override
    public ObjectProcessing getProcessing() {
        return null;
    }

}
