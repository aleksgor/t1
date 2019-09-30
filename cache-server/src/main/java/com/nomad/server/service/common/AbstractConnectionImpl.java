package com.nomad.server.service.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nomad.client.ClientPooledInterface;
import com.nomad.communication.binders.ServerFactory;
import com.nomad.exception.SystemException;
import com.nomad.message.CommonAnswer;
import com.nomad.message.CommonMessage;
import com.nomad.message.MessageSenderReceiver;
import com.nomad.message.MessageSenderReceiverImpl;
import com.nomad.model.CommonClientModel;
import com.nomad.server.DataDefinitionService;
import com.nomad.server.ServerContext;
import com.nomad.utility.AbstractConnection;
import com.nomad.utility.pool.PooledObjectImpl;

public class AbstractConnectionImpl<T extends CommonAnswer, K extends CommonMessage> extends PooledObjectImpl implements AbstractConnection<T, K> {

    protected final ServerContext context;
    private static Logger LOGGER = LoggerFactory.getLogger(AbstractConnectionImpl.class);
    private final ClientPooledInterface<CommonMessage, CommonAnswer> client;
    private final MessageSenderReceiver msr;
    private long size;
    protected final CommonClientModel clientModel;

    public AbstractConnectionImpl(final CommonClientModel clientModel, final ServerContext context) throws SystemException  {
        client = ServerFactory.getPooledClient(clientModel, context);
        final DataDefinitionService dataDefinitionService = context.getDataDefinitionService(null);
        msr = new MessageSenderReceiverImpl(dataDefinitionService);
        this.clientModel = clientModel;
        this.context=context;
    }

    @Override
    @SuppressWarnings("unchecked")
    public T sendMessage(final K message) throws SystemException {
        LOGGER.debug("Pool:{} sendMessage:{}", pool.getPoolId(), message);
        msr.reset();
        //        checkConnect();
        final T answer =(T) client.sendMessage(message);
        LOGGER.debug("Pool: {} getAnswer:{}", pool.getPoolId(), answer);
        return answer;
    }

    @Override
    public void closeObject() {
        client.close();
    }

    @Override
    public long getSize() {
        return size;
    }

    @Override
    public void resetSize() {
        size = 0;

    }

}
