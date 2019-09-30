package com.nomad.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nomad.exception.SystemException;
import com.nomad.message.MessageAssembler;
import com.nomad.server.DataDefinitionService;

public class BinaryZipMessageAssembler extends AbstractMessageHeaderAssembler implements MessageAssembler {
    public BinaryZipMessageAssembler(DataDefinitionService dataDefinitionService) {
        super(dataDefinitionService);
    }

    private final static Logger LOGGER = LoggerFactory.getLogger(BinaryZipMessageAssembler.class);

    @SuppressWarnings("resource")
    @Override
    public void storeObject(Object object, OutputStream output) throws SystemException {

        LOGGER.debug("storeObject object:{}", object);
        MessageOutputStream secondOut = null;
        try {
            GZIPOutputStream zip = new GZIPOutputStream(output);
            secondOut = new MessageOutputStream(zip, dataDefinition);
            secondOut.writeObject(object);
            zip.finish();
            outBytes += secondOut.getBytesCount();
            LOGGER.debug("assemble message:{} successful ", object);
        } catch (IOException e) {
            throw new SystemException(e);
        }

    }

    @Override
    public Object getObject(InputStream input) throws SystemException {
        MessageInputStream secondInput = null;
        try {
            GZIPInputStream zip = new GZIPInputStream(input);
            secondInput = new MessageInputStream(zip, dataDefinition);
            Object object = secondInput.readObject();
            inBytes += secondInput.getBytesCount();
            return object;
        } catch (IOException e) {
            throw new SystemException(e);
        }

    }

}
