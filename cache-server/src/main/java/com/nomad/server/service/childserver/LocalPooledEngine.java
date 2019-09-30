package com.nomad.server.service.childserver;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nomad.cache.commonclientserver.BodyImpl;
import com.nomad.cache.commonclientserver.FullMessageImpl;
import com.nomad.cache.commonclientserver.RawMessageImpl;
import com.nomad.cache.commonclientserver.ResultImpl;
import com.nomad.communication.binders.RawPooledClient;
import com.nomad.core.ObjectProcessingImpl;
import com.nomad.exception.SystemException;
import com.nomad.message.FullMessage;
import com.nomad.message.MessageSenderReceiver;
import com.nomad.message.MessageSenderReceiverImpl;
import com.nomad.message.OperationStatus;
import com.nomad.message.RawMessage;
import com.nomad.message.Result;
import com.nomad.server.DataDefinitionService;
import com.nomad.server.ServerContext;
import com.nomad.server.processing.ObjectProcessing;

public class LocalPooledEngine extends RawPooledClient{

    private ObjectProcessingImpl processing;

    public LocalPooledEngine( ServerContext context) throws SystemException {
        super(null, context,null);
        processing = new ObjectProcessingImpl(context);
    }

    private static Logger LOGGER = LoggerFactory.getLogger(LocalPooledEngine.class);

    @Override
    public RawMessage sendRawMessage(RawMessage message)  throws SystemException{
        DataDefinitionService dataDefinitionService = context.getDataDefinitionService(null);
        MessageSenderReceiver msr = new MessageSenderReceiverImpl(dataDefinitionService);
        try {
            LOGGER.debug("execMessage header:{} message:{}", message.getHeader(), message);
            FullMessage result = new FullMessageImpl(message.getHeader(), msr.getBodyFromByte(message.getMessage()), message.getResult());
            result = processing.execMessage(result);
            LOGGER.debug("execMessage  message:{}", result);

            return new RawMessageImpl(result.getHeader(), msr.getByteFromBody(result.getBody()), result.getResult());
        } catch (Throwable e) {
            Result res= new ResultImpl(OperationStatus.ERROR,e.getMessage());
            try {
                return new RawMessageImpl(message.getHeader(), msr.getByteFromBody(new BodyImpl()),res);
            } catch (Exception e1) {
                LOGGER.error(e1.getMessage(),e1);
            }
        }
        return null;
    }

    @Override
    public void closeObject() {

    }

    @Override
    public ObjectProcessing getProcessing() {
        return processing;
    }
    
}
