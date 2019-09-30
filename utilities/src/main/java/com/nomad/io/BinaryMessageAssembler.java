package com.nomad.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nomad.exception.SystemException;
import com.nomad.message.MessageAssembler;
import com.nomad.server.DataDefinitionService;

public class BinaryMessageAssembler extends AbstractMessageHeaderAssembler implements MessageAssembler {
    public BinaryMessageAssembler(DataDefinitionService dataDefinitionService) {
        super(dataDefinitionService);
    }

    private final static Logger LOGGER = LoggerFactory.getLogger(BinaryMessageAssembler.class);

    @SuppressWarnings("resource")
    @Override
    public void storeObject(Object object, OutputStream output) throws SystemException {
        MessageOutputStream out = new MessageOutputStream(output, dataDefinition);
        try {
            out.writeObject(object);
            out.flush();
            outBytes += out.getBytesCount();
            LOGGER.debug("assemble message:{} successful ", object);
        } catch (IOException e) {
            throw new SystemException(e);
        }

    }

    @SuppressWarnings("resource")
    @Override
    public Object getObject(InputStream is) throws SystemException {
        MessageInputStream input = new MessageInputStream(is, dataDefinition);
        try {
            Object result = input.readObject();
            inBytes += input.getBytesCount();
            return result;
        } catch (IOException e) {
            throw new SystemException(e);
        }
    }

}
