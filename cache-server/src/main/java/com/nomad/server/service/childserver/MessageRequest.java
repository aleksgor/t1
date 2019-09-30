package com.nomad.server.service.childserver;

import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nomad.cache.commonclientserver.BodyImpl;
import com.nomad.cache.commonclientserver.RawMessageImpl;
import com.nomad.cache.commonclientserver.ResultImpl;
import com.nomad.client.RawClientPooledInterface;
import com.nomad.exception.SystemException;
import com.nomad.message.MessageSenderReceiver;
import com.nomad.message.MessageSenderReceiverImpl;
import com.nomad.message.OperationStatus;
import com.nomad.message.RawMessage;

public class MessageRequest implements Callable<MessageAnswer> {
    private static Logger LOGGER = LoggerFactory.getLogger(MessageRequest.class);

    private final StoreConnectionPool pool;
    private final RawMessage message;

    public MessageRequest (final RawMessage message, final StoreConnectionPool pool ){
        this.pool=pool;
        this.message=message;
    }

    @Override
    public MessageAnswer call() throws SystemException  {
        LOGGER.debug("send message {} to {}",message, pool.getPoolId());
        final RawMessage result = executeMessage();
        return new MessageAnswer(result,pool);
    }

    public RawMessage executeMessage() throws SystemException {
        final RawClientPooledInterface client = pool.getObject();
        RawMessage result=null;
        if(client!=null){
            try{
                result=client.sendRawMessage( message);
                LOGGER.debug("take message {} from {}",message, pool.getPoolId());
            }catch(final Exception e){
                LOGGER.error(e.getMessage(),e);
            }finally{
                client.freeObject();
            }
        }
        if(result==null){ // in error case
            LOGGER.error("cannot conect to server:"+pool.getConnectModel());
            final MessageSenderReceiver msr= new MessageSenderReceiverImpl(message.getHeader().getVersion(),pool.getDataDefinitionService());
            byte[] data = new byte[0];
            try {
                data=msr.getByteFromBody(new BodyImpl());
            } catch (final Exception e) {
                LOGGER.error(e.getMessage(), e);
            }
            result= new RawMessageImpl(message.getHeader(),  data , new ResultImpl(OperationStatus.ERROR,"cannot conect to server:"+pool.getConnectModel()));
        }
        return result;
    }

    public RawMessage getMessage() {
        return message;
    }

}
