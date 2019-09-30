package com.nomad.message;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.nomad.cache.commonclientserver.BodyImpl;
import com.nomad.cache.commonclientserver.RawMessageImpl;
import com.nomad.exception.SystemException;
import com.nomad.io.AbstractMessageHeaderAssembler;
import com.nomad.io.BinaryMessageAssembler;
import com.nomad.server.DataDefinitionService;

public class MessageSenderReceiverImpl extends AbstractMessageHeaderAssembler implements MessageSenderReceiver {

    private byte messageVersion = 0x1;
    private final MessageAssembler assembler;

    public MessageSenderReceiverImpl(final DataDefinitionService dataDefinitionService) {
        super(dataDefinitionService);
        assembler = new BinaryMessageAssembler(dataDefinitionService);
    }

    public MessageSenderReceiverImpl(final byte version, final DataDefinitionService dataDefinitionService) {
        this(dataDefinitionService);
        messageVersion = version;
    }

    @Override
    public MessageHeader getMessageHeader(final InputStream is) throws SystemException {
        final MessageHeader header = parseHeader(is);
        if (header == null) {
            return null;
        }
        setVersion(header.getVersion());

        LOGGER.debug("Get message header:{}", header);
        return header;
    }

    @Override
    public void assembleMessageHeader(final MessageHeader header, final OutputStream output) throws SystemException {
        LOGGER.debug("assembleMessageHeader:{} ", header);
        setVersion(header.getVersion());
        assembleHeader(header, output);
        try {
            output.flush();
        } catch (IOException e) {
            throw new SystemException(e);
        }

    }

    @Override
    public void assembleBody(final Body body, final OutputStream output) throws SystemException {
        LOGGER.debug("assembleBody:{} ", body);
        final ByteArrayOutputStream data = new ByteArrayOutputStream();
        try {
            assembler.storeObject(body, data);
            final byte[] bytes = data.toByteArray();
            writeInt(bytes.length, output);
            output.write(bytes);

            output.flush();
        } catch (IOException e) {
            throw new SystemException(e);
        }

    }

    public void assembleData(final Object message, final OutputStream output) throws SystemException {
        LOGGER.debug("assembleData:{} ", message);
        final ByteArrayOutputStream data = new ByteArrayOutputStream();
        try {
            assembler.storeObject(message, data);
            final byte[] bytes = data.toByteArray();
            writeInt(bytes.length, output);
            output.write(bytes);
            output.flush();
        } catch (IOException e) {
            throw new SystemException(e);
        }

    }

    @Override
    public void storeObject(final Object object, final OutputStream output) throws SystemException {
        final byte version = messageVersion;
        try {
            output.write(version);
            assembler.storeObject(object, output);
            outBytes += assembler.getOutBytes();
            output.flush();
            assembler.reset();
        } catch (IOException e) {
            throw new SystemException(e);
        }

    }

    @Override
    public Object getObject(final InputStream is) throws SystemException {
        try {
            setVersion((byte) is.read());
            final Object result = assembler.getObject(is);
            inBytes += assembler.getInBytes() + 1;
            assembler.reset();
            return result;
        } catch (IOException e) {
            throw new SystemException(e);
        }
    }

    @Override
    public byte getMessageVersion() {
        return messageVersion;
    }

    @Override
    public Body getBody(final InputStream is) throws SystemException {
        readInt(is);
        final Body result = (Body) assembler.getObject(is);
        LOGGER.debug("getModelMessage:{}", result);
        if (result == null) {
            LOGGER.error("body bug");

        }
        return result;
    }

    @Override
    public Object getData(final InputStream is) throws SystemException {
        readInt(is);
        final Object result = assembler.getObject(is);
        LOGGER.debug("getModelMessage:{}", result);
        if (result == null) {
            LOGGER.warn("data bug");
        }
        return result;
    }

    private void setVersion(final byte version) {
        if (version != messageVersion) {
            messageVersion = version;
        }

    }

    @Override
    public Result getResult(final InputStream is) throws SystemException {

        return parseResult(is);
    }

    @Override
    public RawMessage getRawMessage(final InputStream is) throws SystemException {

        final MessageHeader header = parseHeader(is);
        setVersion(header.getVersion());

        final Result res = parseResult(is);
        final byte[] message = readByteBody(is);
        LOGGER.debug("getRawMessage:{}", message);
        return new RawMessageImpl(header, message, res);
    }

    @Override
    public void assembleRawMessage(final RawMessage message, final OutputStream output) throws SystemException {
        setVersion(message.getHeader().getVersion());
        LOGGER.debug("assembleRawMessage version:'{}'", messageVersion);
        assembleHeader(message.getHeader(), output);
        assembleResult(message.getResult(), output);
        try{
        writeInt(message.getMessage().length, output);
        output.write(message.getMessage());
        output.flush();
        LOGGER.debug("assembleRawMessage:{}", message);
        } catch (IOException e) {
            throw new SystemException(e);
        }

    }

    @Override
    public byte[] getByteFromBody(final Body message) throws SystemException {
        LOGGER.debug("assembleBody:{} ", message);
        final ByteArrayOutputStream data = new ByteArrayOutputStream();
        assembler.storeObject(message, data);
        return data.toByteArray();
    }

    @Override
    public Body getBodyFromByte(final byte[] data) throws SystemException {
        final ByteArrayInputStream input = new ByteArrayInputStream(data);
        final Body result = (Body) assembler.getObject(input);
        LOGGER.debug("getModelMessage:{}", result);
        if (result == null) {
            LOGGER.error("body from byte bug");

        }
        return result;
    }

    private byte[] readByteBody(final InputStream input) throws SystemException {
        int length = readInt(input);

        final ByteArrayOutputStream data = new ByteArrayOutputStream(length);
        final byte[] buffer = new byte[512];
        try {
            while (length > 0) {
                final int read = input.read(buffer, 0, buffer.length);
                data.write(buffer, 0, read);
                length -= read;
            }
            inBytes += length;
            return data.toByteArray();
        } catch (IOException e) {
            throw new SystemException(e);
        }

    }

    @Override
    public byte[] getEmptyBody() throws SystemException {
        return getByteFromBody(new BodyImpl());
    }

    @Override
    public void reset() {
        outBytes = 0;
        inBytes = 0;
    }

}
