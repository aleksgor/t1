package com.nomad.server.service.session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nomad.client.ClientPooledInterface;
import com.nomad.exception.SystemException;
import com.nomad.model.session.SessionAnswer;
import com.nomad.model.session.SessionMessage;
import com.nomad.server.ServerContext;
import com.nomad.utility.pool.PooledObjectImpl;

public class  SessionConnection <T extends  SessionMessage ,K extends SessionAnswer > extends PooledObjectImpl {

    protected final ServerContext context;
    private static Logger LOGGER = LoggerFactory.getLogger(SessionConnection.class);
    private final ClientPooledInterface<T,K> client;

    public SessionConnection(final ClientPooledInterface<T,K> client , final ServerContext context) {
        this.client=client;
        this.context=context;
    }

    public SessionAnswer sendMessage(final T message) throws SystemException  {
        LOGGER.debug("Pool:{} sendMessage:{}",pool.getPoolId(),message);
        final K answer =client.sendMessage(message);
        LOGGER.debug("Pool: {} getAnswer:{}",pool.getPoolId(),answer);
        return answer;
    }

    @Override
    public void closeObject() {
        client.closeObject();
    }

    @Override
    protected long getSize() {
        // TODO Auto-generated method stub
        return 0;
    }

}
