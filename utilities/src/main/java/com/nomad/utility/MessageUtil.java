package com.nomad.utility;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nomad.cache.commonclientserver.BodyImpl;
import com.nomad.exception.AccessException;
import com.nomad.exception.BlockException;
import com.nomad.exception.EOMException;
import com.nomad.exception.LogicalException;
import com.nomad.exception.ModelNotExistException;
import com.nomad.exception.RelationNotExistException;
import com.nomad.exception.SystemException;
import com.nomad.exception.UnsupportedModelException;
import com.nomad.exception.UpdateModelException;
import com.nomad.message.MessageHeader;
import com.nomad.message.MessageSenderReceiver;
import com.nomad.message.MessageSenderReceiverImpl;
import com.nomad.message.OperationStatus;
import com.nomad.model.Identifier;
import com.nomad.model.Model;
import com.nomad.model.core.SessionContainer;
import com.nomad.model.criteria.StatisticResult;
import com.nomad.model.criteria.StatisticResultImpl;

public class MessageUtil {
    protected static Logger LOGGER = LoggerFactory.getLogger(MessageUtil.class);

    public static String readData(final InputStream in) {
        final ByteArrayOutputStream data = new ByteArrayOutputStream();
        try {
            final byte[] buff = new byte[512];
            int count;
            while (in.available() > 0) {
                count = in.read(buff);
                data.write(buff, 0, count);
            }
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(),e);
        }
        return new String(data.toByteArray());
    }

    public static MessageHeader getHeaderCopy(final MessageHeader header) {
        final MessageHeader result = new MessageHeader();
        result.setCommand(header.getCommand());
        result.setModelName(header.getModelName());
        result.setSessionId(header.getSessionId());
        result.setVersion(header.getVersion());
        result.getSessions().addAll(header.getSessions());
        result.setMainSession(header.getMainSession());
        result.setUserName(header.getUserName());
        result.setPassword(header.getPassword());
        return result;

    }

    public static MessageHeader getHeaderCopy(final MessageHeader header, String command) {
        final MessageHeader result = getHeaderCopy(header);
        result.setCommand(command);
        return result;

    }

    public static byte[] getEmptyBody(final byte messageVersion) {
        try {
            return new MessageSenderReceiverImpl(messageVersion,null).getByteFromBody(new BodyImpl());
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(),e);
        }
        return new byte[0];
    }

    private static byte[] emptyBody;

    public static byte[] getEmptyBody() {
        if (emptyBody == null) {
            try {
                emptyBody = (new MessageSenderReceiverImpl((byte) 0x1, null).getByteFromBody(new BodyImpl()));
            } catch (final Exception e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
        return emptyBody;
    }

    public static StatisticResult<Model> getStatisticResult(Collection<Identifier> ids) {
        StatisticResult<Model> result = new StatisticResultImpl<>();
        result.setIdentifiers(ids);
        return result;

    }

    public static StatisticResult<Model> getStatisticResult(int count) {
        StatisticResult<Model> result = new StatisticResultImpl<>();
        result.setCountAllRow(count);
        return result;
    }

    public static Model getModelFromBytes(final byte[] input, final MessageSenderReceiver msr) throws SystemException {
        if (input == null) {
            return null;
        }
        try {
            return (Model)msr.getObject(new ByteArrayInputStream(input));
        } catch (final Exception e) {
            throw new SystemException(e.getMessage(), e);
        }
    }

    public static byte[] getBytesFromModel(final Model data, final MessageSenderReceiver msr) throws SystemException {
        if (data == null) {
            return null;
        }
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            msr.storeObject(data, outputStream);
            outputStream.flush();
            outputStream.close();
            return outputStream.toByteArray();
        } catch (final Exception e) {
            throw new SystemException(e.getMessage(), e);
        }
    }

    public static Model clone(Model data, final MessageSenderReceiver msr) throws SystemException {
        return getModelFromBytes(getBytesFromModel(data,msr),msr);
    }

    @Deprecated
    public static Object getObjectFromBytes(final byte[] input) throws SystemException {
        if (input == null) {
            return null;
        }
        try {
            ObjectInputStream objectInputStream = new ObjectInputStream(new ByteArrayInputStream(input));
            return objectInputStream.readObject();
        } catch (final Exception e) {
            throw new SystemException(e.getMessage(), e);
        }
    }

    @Deprecated
    public static byte[] getBytesFromObject(final Object data) throws SystemException {
        if (data == null) {
            return null;
        }
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        try {
            final ObjectOutputStream outStream = new ObjectOutputStream(bout);
            outStream.writeObject(data);
            outStream.flush();
            outStream.close();
            return bout.toByteArray();
        } catch (final IOException e) {
            throw new SystemException(e.getMessage(), e);
        }
    }

    @Deprecated
    public static Object clone(Object data) throws SystemException {
        return getObjectFromBytes(getBytesFromObject(data));
    }

    public static void applySessions(MessageHeader header, SessionContainer sessions) {
        header.setSessionId(sessions.getSessionId());
        header.setMainSession(sessions.getMainSessionId());
        header.getSessions().clear();
        header.getSessions().addAll(sessions.getSessions());
    }
    
    public static OperationStatus getStatusByException(LogicalException e){
        if (e instanceof AccessException) {
            return OperationStatus.ACCESS_DENIED;
        }else if(e instanceof BlockException) {
            return OperationStatus.BLOCKED;
        }else if(e instanceof ModelNotExistException) {
            return OperationStatus.MODEL_NOT_EXIST;
        }else if(e instanceof RelationNotExistException) {
            return OperationStatus.RELATION_NOT_EXIST;
        }else if(e instanceof UnsupportedModelException) {
            return OperationStatus.UNSUPPORTED_MODEL_NAME;
        }else if(e instanceof UpdateModelException) {
            return OperationStatus.UPDATE_MODEL_ERROR;
        }
        return OperationStatus.ERROR;
    }
    
    public static byte[] readByteBody(final InputStream input) throws SystemException, EOMException {
        try{
        final int chunk1 = input.read();
        final int chunk2 = input.read();
        final int chunk3 = input.read();
        final int chunk4 = input.read();
        if ((chunk1 | chunk2 | chunk3 | chunk4) < 0) {
            throw new EOMException();
        }
        int length = ((chunk1 << 24) + (chunk2 << 16) + (chunk3 << 8) + (chunk4 << 0));
        final ByteArrayOutputStream data = new ByteArrayOutputStream(length);
        final byte[] buffer = new byte[512];
        while (length > 0) {
            final int read = input.read(buffer, 0, buffer.length);
            data.write(buffer, 0, read);
            length -= read;
        }
        return data.toByteArray();
        } catch (IOException  e){
            throw new SystemException(e);
        }
    }
}
