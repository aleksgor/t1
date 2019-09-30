package com.nomad.io;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nomad.cache.commonclientserver.ResultImpl;
import com.nomad.exception.ErrorCodes;
import com.nomad.exception.SystemException;
import com.nomad.io.serializer.SerializerFactory;
import com.nomad.io.serializer.SerializerPooledObject;
import com.nomad.message.MessageHeader;
import com.nomad.message.Result;
import com.nomad.server.DataDefinitionService;

/**
 * format of message VVVLLLcommand:SessionId:ModelName:ModelId
 *
 * @author polzovatel
 *
 */
public abstract class AbstractMessageHeaderAssembler {
    protected static Logger LOGGER = LoggerFactory.getLogger(AbstractMessageHeaderAssembler.class);

    protected long inBytes = 0;
    protected long outBytes = 0;
    protected final DataDefinitionService dataDefinition;

    public AbstractMessageHeaderAssembler(final DataDefinitionService dataDefinitionService) {
        dataDefinition = dataDefinitionService;
    }

    @SuppressWarnings("unchecked")
    public MessageHeader parseHeader(final InputStream is) throws SystemException {
        final MessageHeader result = new MessageHeader();

        @SuppressWarnings("resource")
        final MessageInputStream reader = new MessageInputStream(is, dataDefinition);
        try {
            final byte version = (byte) reader.read();
            if (version < 0) {
                throw new SystemException(ErrorCodes.Connect.ERROR_CONNECT_EOF);
            }
            result.setVersion(version);
            result.setModelName(reader.readString());
            result.setCommand(reader.readString());
            result.setSessionId(reader.readString());
            result.setMainSession(reader.readString());
            result.getSessions().addAll((Collection<? extends String>) reader.readList());
            result.setUserName(reader.readString());
            result.setPassword(reader.readString());

            inBytes += reader.getBytesCount();
            LOGGER.debug("parseHeader:{}", result);
        } catch (IOException e) {
            throw new SystemException(e);
        }
        return result;
    }

    @SuppressWarnings("resource")
    public void assembleHeader(final MessageHeader header, final OutputStream output) throws SystemException {
        LOGGER.debug("assemble header:{}", header);
        final MessageOutputStream writer = new MessageOutputStream(output, dataDefinition);

        try {
            writer.writeByte(header.getVersion());
            writer.writeString(header.getModelName());
            writer.writeString(header.getCommand());
            writer.writeString(header.getSessionId());
            writer.writeString(header.getMainSession());
            writer.writeList(header.getSessions());
            writer.writeString(header.getUserName());
            writer.writeString(header.getPassword());
            writer.flush();
        } catch (IOException e) {
            throw new SystemException(e);
        }
        outBytes += writer.getBytesCount();
        LOGGER.debug("assemble header:{} successful ", header);
    }

    protected String readString(final InputStream is, final int count) {
        try {
            final byte[] bytes = readBytes(is, count);
            final String result = new String(bytes, "UTF-8");
            return result;
        } catch (final Exception x) {
            LOGGER.error(x.getMessage(), x);
        }
        return "";
    }

    protected byte[] readBytes(final InputStream is, final int count) {
        inBytes += count;

        final byte[] result = new byte[count];

        try {
            int read = 0;
            int r = 0;
            int maxLength = count;
            while (read < count) {

                r = is.read(result, read, maxLength);
                if (r < 0) {
                    throw new EOFException();
                }
                read += r;
                maxLength = count - read;
            }
        } catch (final IOException e) {

        }
        return result;
    }

    @SuppressWarnings("resource")
    protected Result parseResult(final InputStream is) throws  SystemException {
        LOGGER.debug("parse result:");

        final SerializerPooledObject<Object> serializer = SerializerFactory.getSerializer(ResultImpl.class.getName());
        try {
            final MessageInputStream input = new MessageInputStream(is, dataDefinition);
            final Result result = (Result) input.readObject();
            LOGGER.debug("parse result:{}", result);
            inBytes += input.getBytesCount();
            return result;
        } catch (IOException e) {
            throw new SystemException(e);
        } finally {
            serializer.freeObject();
        }
    }

    public void assembleResult(final Result result, final OutputStream output) throws SystemException {

        LOGGER.debug("assemble result:{}", result);
        final SerializerPooledObject<Object> serializer = SerializerFactory.getSerializer(ResultImpl.class.getName());
        try {
            @SuppressWarnings("resource")
            final MessageOutputStream messageOutput = new MessageOutputStream(output, dataDefinition);
            messageOutput.writeObject(result);
            outBytes += messageOutput.getBytesCount();
            output.flush();
        } catch (IOException e) {
            throw new SystemException(e);
        } finally {
            serializer.freeObject();
        }

    }

    protected int readInt(final InputStream input) throws SystemException {
        try {
            int char1;
            char1 = input.read();
            final int char2 = input.read();
            final int char3 = input.read();
            final int char4 = input.read();
            inBytes += 4;
            if ((char1 | char2 | char3 | char4) < 0)
                throw new SystemException();
            return ((char1 << 24) + (char2 << 16) + (char3 << 8) + (char4 << 0));
        } catch (IOException e) {
            throw new SystemException(e);
        }
    }

    protected void writeInt(final int v, final OutputStream out) throws IOException {
        out.write((v >>> 24) & 0xFF);
        out.write((v >>> 16) & 0xFF);
        out.write((v >>> 8) & 0xFF);
        out.write((v >>> 0) & 0xFF);
        outBytes += 4;
    }

    public long getInBytes() {
        return inBytes;
    }

    public long getOutBytes() {
        return outBytes;
    }

    public void reset() {
        inBytes = 0;
        outBytes = 0;
    }
}
