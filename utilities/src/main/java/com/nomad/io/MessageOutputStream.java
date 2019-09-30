package com.nomad.io;

import java.beans.PropertyDescriptor;
import java.io.ByteArrayOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.io.UTFDataFormatException;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nomad.exception.SystemException;
import com.nomad.io.serializer.SerializerFactory;
import com.nomad.io.serializer.SerializerPooledObject;
import com.nomad.model.Field;
import com.nomad.model.Identifier;
import com.nomad.model.Model;
import com.nomad.model.ModelDescription;
import com.nomad.model.Relation;
import com.nomad.model.criteria.AbstractCriteria;
import com.nomad.server.DataDefinitionService;

public class MessageOutputStream extends FilterOutputStream {
    private static final Logger LOGGER = LoggerFactory.getLogger(MessageOutputStream.class);
    private long bytesCount = 0;
    private final DataDefinitionService dataDefinition;

    public MessageOutputStream(final OutputStream output, final DataDefinitionService dataDefinitionService) {
        super(output);
        dataDefinition = dataDefinitionService;
    }

    /**
     * Write an int value to an output stream
     *
     * @param outputStream
     * @param value
     * @throws IOException
     */
    public void writeShort(final short value) throws IOException {
        out.write((value >>> 8) & 0xFF);
        out.write((value >>> 0) & 0xFF);
        bytesCount += 2;
    }

    /**
     * Write an int value to an output stream
     *
     * @param outputStream
     * @param value
     * @throws IOException
     */
    public void writeTYPE(final MessageType value) throws IOException {
        out.write(value.getCode());
        bytesCount++;
    }

    /**
     * Write a byte array to an output stream only with its raw content.
     *
     * @param dos
     * @param bytes
     *          , it can not be null
     * @throws IOException
     */
    public void writeRawBytes(final byte[] bytes) throws IOException {
        assert bytes != null;
        write(bytes);
        bytesCount += bytes.length;

    }

    // ------------for object read/write-------------------

    private static Map<Class<?>, MessageType> type2IndexMap;

    static {
        type2IndexMap = new HashMap<>();
        type2IndexMap.put(Integer.class, MessageType.TYPE_INT);
        type2IndexMap.put(Short.class, MessageType.TYPE_SHORT);
        type2IndexMap.put(Byte.class, MessageType.TYPE_BYTE);
        type2IndexMap.put(Long.class, MessageType.TYPE_LONG);
        type2IndexMap.put(Float.class, MessageType.TYPE_FLOAT);
        type2IndexMap.put(Double.class, MessageType.TYPE_DOUBLE);
        type2IndexMap.put(BigDecimal.class, MessageType.TYPE_BIG_DECIMAL);
        type2IndexMap.put(Date.class, MessageType.TYPE_DATE_TIME);
        type2IndexMap.put(Boolean.class, MessageType.TYPE_BOOLEAN);
        type2IndexMap.put(String.class, MessageType.TYPE_STRING);
        type2IndexMap.put(byte[].class, MessageType.TYPE_BYTES);
        type2IndexMap.put(List.class, MessageType.TYPE_LIST);
        type2IndexMap.put(Map.class, MessageType.TYPE_MAP);
        type2IndexMap.put(Serializable.class, MessageType.TYPE_SERIALIZABLE);
        //    type2IndexMap.put(null, MessageType.TYPE_NULL);
        //    type2IndexMap.put(null, MessageType.TYPE_MODEL);
        //   type2IndexMap.put(null, MessageType.TYPE_IDENTIFIER);

    }

    /**
     * from object class to its type index value
     * @param objectValue
     * @return
     */
    private MessageType getTypeIndex(final Object objectValue) {
        if (objectValue == null)
            return MessageType.TYPE_NULL;

        if (objectValue instanceof String) {
            return MessageType.TYPE_STRING;
        }

        final MessageType indexObject = type2IndexMap.get(objectValue.getClass());


        if (indexObject == null) {
            if (objectValue instanceof Map) {
                return MessageType.TYPE_MAP;
            } else if (objectValue instanceof List) {
                return MessageType.TYPE_LIST;
            }
            if (objectValue instanceof Model) {
                if (dataDefinition.getModelDescription(((Model) objectValue).getModelName()) != null) {
                    return MessageType.TYPE_MODEL;
                }
            }
            if (objectValue instanceof Identifier) {
                if (dataDefinition.getModelDescription(((Identifier) objectValue).getModelName()) != null) {
                    return MessageType.TYPE_IDENTIFIER;
                }
                return MessageType.TYPE_LIST;
            }
            if (Date.class.isAssignableFrom(objectValue.getClass())) {
                return MessageType.TYPE_DATE_TIME;
            }
            if (objectValue instanceof Enum) {
                return MessageType.TYPE_ENUM;
            }
            return null;
        }
        return indexObject;
    }

    /**
     * When obValue is not supported te be serialized, an IOException will be thrown.
     * @param dos
     * @param objectValue
     * @throws IOException
     */
    @SuppressWarnings("unchecked")
    public void writeObject(final Object objectValue) throws IOException, SystemException {
        // write data type index first

        if (objectValue == null) {
            writeTYPE(MessageType.TYPE_NULL);
            return;
        }
        SerializerPooledObject<Object> serializer = null;
        String className = null;
        MessageType typeIndex = getTypeIndex(objectValue);
        if (typeIndex == null) {
            serializer = SerializerFactory.getSerializer(objectValue.getClass().getName());
            if (serializer != null) {
                typeIndex = MessageType.TYPE_MANUAL_SERIALIZABLE;
                className = objectValue.getClass().getName();
            } else if (objectValue instanceof AbstractCriteria) {
                typeIndex = MessageType.TYPE_MANUAL_SERIALIZABLE;
                className = AbstractCriteria.class.getName();
                serializer = SerializerFactory.getSerializer( AbstractCriteria.class.getName());
            }
        }
        if (typeIndex == null) {
            writeTYPE(MessageType.TYPE_NULL);
            throw new IOException("Data type of " + objectValue.getClass().toString() + " is not supported to be serialized");
        }
        writeTYPE(typeIndex);
        // write real data
        switch (typeIndex) {
        case TYPE_NULL:
            break;
        case TYPE_INT:
            writeInteger(((Integer) objectValue).intValue());
            break;
        case TYPE_SHORT:
            writeShort(((Short) objectValue).shortValue());
            break;
        case TYPE_BYTE:
            write(((Byte) objectValue).byteValue());
            break;
        case TYPE_FLOAT:
            writeFloat(((Float) objectValue).floatValue());
            break;
        case TYPE_DOUBLE:
            writeDouble(((Double) objectValue).doubleValue());
            break;
        case TYPE_LONG:
            writeLong(((Long) objectValue).longValue());
            break;
        case TYPE_BIG_DECIMAL:
            writeUTF(((BigDecimal) objectValue).toString());
            break;
        case TYPE_DATE_TIME:
            writeLong(((Date) objectValue).getTime());
            break;
        case TYPE_BOOLEAN:
            writeBoolean(((Boolean) objectValue).booleanValue());
            break;
        case TYPE_STRING:
            writeUTF(objectValue.toString());
            break;
        case TYPE_BYTES:
            byte[] bytes = (byte[]) objectValue;
            final int length = bytes.length;
            writeInteger(length);
            if (length > 0)
                write(bytes);
            bytesCount += length;
            break;
        case TYPE_LIST:
            writeList((List<Object>) objectValue);
            break;
        case TYPE_MAP:
            writeMap((Map<Object, Object>) objectValue);
            break;
        case TYPE_ARRAY_INT:
            writeArrayInteger((int[]) objectValue);
            break;
        case TYPE_ARRAY_LONG:
            writeArrayLong((long[]) objectValue);
            break;
        case TYPE_ARRAY_STRING:
            writeArrayString((String[]) objectValue);
            break;
        case TYPE_SERIALIZABLE:
            bytes = null;
            LOGGER.warn("native serializaion: " + objectValue.getClass().getName());

            try {
                final ByteArrayOutputStream buff = new ByteArrayOutputStream();
                final ObjectOutputStream objectOutputStream = new ObjectOutputStream(buff);
                objectOutputStream.writeObject(objectValue);
                objectOutputStream.close();
                bytes = buff.toByteArray();
                bytesCount += bytes.length;
            } catch (final Exception ex) {
            }
            if (bytes == null || bytes.length == 0) {
                writeInteger(0);
            } else {
                writeInteger(bytes.length);
                write(bytes);
            }
            break;
        case TYPE_MANUAL_SERIALIZABLE:
            writeUTF(className);
            serializer.getSerializer().write(this, objectValue);
            serializer.freeObject();
            break;
        case TYPE_MODEL:
            writeModel((Model) objectValue);
            break;
        case TYPE_IDENTIFIER:
            writeIdentifier((Identifier) objectValue);
            break;
        case TYPE_ENUM:
            writeUTF(((Enum<?>) objectValue).name());
            break;

        }
    }

    public void writeModel(final Model model) throws SystemException {

        final ModelDescription description = dataDefinition.getModelDescription(model.getModelName());

        if (description == null) {
            throw new SystemException("model " + model.getModelName() + " not registred!");
        }
        try {
            writeUTF(model.getModelName());

            final Identifier id = model.getIdentifier();
            if (id == null) {

            }
            if (id == null) {
                out.write(MessageType.TYPE_NULL.getCode());
            } else {
                out.write(MessageType.TYPE_IDENTIFIER.getCode());
                for (final Field field : description.getPrimaryKeyFields()) {
                    final Object data = new PropertyDescriptor(field.getName(), id.getClass()).getReadMethod().invoke(id);
                    writeObject(data);
                }
            }

            for (final Field field : description.getFields().values()) {
                final Object data = new PropertyDescriptor(field.getName(), model.getClass()).getReadMethod().invoke(model);
                writeObject(data);
            }
            // relations
            for (final Relation relation : description.getRelations().values()) {
                final Object data = new PropertyDescriptor(relation.getFieldName(), model.getClass()).getReadMethod().invoke(model);
                writeObject(data);
            }
        } catch (final Exception e) {
            throw new SystemException(e.getMessage(), e);
        } finally {
            try {
                out.flush();
            } catch (final IOException e) {
                LOGGER.error(e.getMessage(), e);
            }
        }

    }

    public void writeIdentifier(final Identifier identifier) throws SystemException {

        final ModelDescription description = dataDefinition.getModelDescription(identifier.getModelName());
        if (description == null) {
            throw new SystemException(" model:" + identifier.getModelName() + " not registred!");
        }

        try {

            writeUTF(identifier.getModelName());

            for (final Field field : description.getPrimaryKeyFields()) {
                final Object data = new PropertyDescriptor(field.getName(), identifier.getClass()).getReadMethod().invoke(identifier);
                writeObject(data);
            }

        } catch (final Exception e) {
            throw new SystemException(e.getMessage(), e);
        } finally {
            try {
                out.flush();
            } catch (final IOException e) {
                LOGGER.error(e.getMessage(), e);
            }
        }

    }

    private void writeArrayInteger(final int[] integerArray) throws IOException {
        if (integerArray == null) {
            writeTYPE(MessageType.TYPE_NULL);
            return;
        } else {
            writeTYPE(MessageType.TYPE_ARRAY_INT);
        }

        // write map size
        final int size = integerArray.length;
        writeInteger(size);
        if (size == 0)
            return;

        // write real data
        for (int i = 0; i < size; i++)
            writeInteger(integerArray[i]);
    }

    private void writeArrayLong(final long[] longArray) throws IOException {
        if (longArray == null) {
            writeTYPE(MessageType.TYPE_NULL);
            return;
        } else {
            writeTYPE(MessageType.TYPE_ARRAY_LONG);
        }

        // write map size
        final int size = longArray.length;
        writeInteger(size);
        if (size == 0)
            return;

        // write real data
        for (int i = 0; i < size; i++)
            writeLong(longArray[i]);
    }

    private void writeArrayString(final String[] stringArray) throws IOException {
        if (stringArray == null) {
            writeTYPE(MessageType.TYPE_NULL);
            return;
        } else {
            writeTYPE(MessageType.TYPE_ARRAY_STRING);
        }

        // write map size
        final int size = stringArray.length;
        writeInteger(size);
        if (size == 0)
            return;

        // write real data
        for (int i = 0; i < size; i++)
            writeString(stringArray[i]);
    }

    /**
     * Write a String value to an output stream
     * @param outputStream
     * @param string
     * @throws IOException
     */
    public void writeString(final String string) throws IOException {
        if (string == null) {
            writeTYPE(MessageType.TYPE_NULL);
            return;
        } else {
            writeTYPE(MessageType.TYPE_STRING);
            writeUTF(string);
        }
    }

    /**
     * Write a bytes to an output stream
     *
     * @param dos
     * @param dataMap
     * @throws IOException
     * @throws BirtException
     */
    public void writeBytes(final byte[] bytes) throws IOException {
        // check null
        if (bytes == null) {
            writeTYPE(MessageType.TYPE_NULL);
            return;
        }

        writeTYPE(MessageType.TYPE_BYTES);

        // write byte size and its content
        final int size = bytes.length;
        writeInteger(size);
        if (size == 0)
            return;
        write(bytes);
    }

    /**
     * Write a list to an output stream
     *
     * @param dos
     * @param dataMap
     * @throws IOException
     * @throws BirtException
     */
    public void writeList(final Collection<? extends Object> list) throws IOException, SystemException {
        if (list == null) {
            writeTYPE(MessageType.TYPE_NULL);
            return;
        } else {
            writeTYPE(MessageType.TYPE_LIST);
        }

        // write map size
        final int size = list.size();
        writeInteger(size);
        if (size == 0)
            return;

        // write real data
        final Iterator<? extends Object> iterator= list.iterator();
        for (; iterator.hasNext(); )
            writeObject(iterator.next());
    }


    /**
     * Write a Map to an output stream
     *
     * @param dos
     * @param map
     * @throws IOException
     * @throws BirtException
     */
    public void writeMap(final Map<? extends Object, ? extends Object> map) throws IOException, SystemException {
        // check null
        if (map == null) {
            writeTYPE(MessageType.TYPE_NULL);
            return;
        } else {
            writeTYPE(MessageType.TYPE_MAP);
        }

        // write map size
        final int size = map.size();
        writeInteger(size);
        if (size == 0)
            return;

        // write real data
        @SuppressWarnings({ "unchecked", "rawtypes" })
        final
        Iterator<Map.Entry<Object, Object>> it = (Iterator) map.entrySet().iterator();
        while (it.hasNext()) {
            final Map.Entry<Object, Object> entry = it.next();
            final Object key = entry.getKey();
            final Object value = entry.getValue();
            writeObject(key);
            writeObject(value);
        }
    }

    /**
     * private utility method to the size of a string in bytes
     * @param string
     * @throws UTFDataFormatException
     */
    private int getBytesSize(final String string) {
        int c, utfLength = 0;
        for (int i = 0; i < string.length(); i++) {
            c = string.charAt(i);
            if ((c >= 0x0001) && (c <= 0x007F)) {
                utfLength++;
            } else if (c > 0x07FF) {
                utfLength += 3;
            } else {
                utfLength += 2;
            }
        }
        return utfLength;
    }

    public void writeInteger(final int v) throws IOException {
        out.write((v >>> 24) & 0xFF);
        out.write((v >>> 16) & 0xFF);
        out.write((v >>> 8) & 0xFF);
        out.write((v >>> 0) & 0xFF);
        bytesCount += 4;
    }

    public final void writeFloat(final float v) throws IOException {
        writeInteger(Float.floatToIntBits(v));
    }

    public final void writeDouble(final double v) throws IOException {
        writeLong(Double.doubleToLongBits(v));
    }

    public final void writeLong(final long v) throws IOException {
        final byte writeBuffer[] = new byte[8];
        writeBuffer[0] = (byte) (v >>> 56);
        writeBuffer[1] = (byte) (v >>> 48);
        writeBuffer[2] = (byte) (v >>> 40);
        writeBuffer[3] = (byte) (v >>> 32);
        writeBuffer[4] = (byte) (v >>> 24);
        writeBuffer[5] = (byte) (v >>> 16);
        writeBuffer[6] = (byte) (v >>> 8);
        writeBuffer[7] = (byte) (v >>> 0);
        out.write(writeBuffer, 0, 8);
        bytesCount += 8;
    }

    int writeUTF(final String string) throws IOException {
        final int stringLength = string.length();
        int c, count = 0;

        /* use charAt instead of copying String to char array */

        final int utfLength = getBytesSize(string);

        bytesCount += utfLength;

        byte[] byteArray = null;
        byteArray = new byte[utfLength];

        writeInteger(utfLength);

        int i = 0;
        for (i = 0; i < stringLength; i++) {
            c = string.charAt(i);
            if (!((c >= 0x0001) && (c <= 0x007F)))
                break;
            byteArray[count++] = (byte) c;
        }

        for (; i < stringLength; i++) {
            c = string.charAt(i);
            if ((c >= 0x0001) && (c <= 0x007F)) {
                byteArray[count++] = (byte) c;

            } else if (c > 0x07FF) {
                byteArray[count++] = (byte) (0xE0 | ((c >> 12) & 0x0F));
                byteArray[count++] = (byte) (0x80 | ((c >> 6) & 0x3F));
                byteArray[count++] = (byte) (0x80 | ((c >> 0) & 0x3F));
            } else {
                byteArray[count++] = (byte) (0xC0 | ((c >> 6) & 0x1F));
                byteArray[count++] = (byte) (0x80 | ((c >> 0) & 0x3F));
            }
        }
        out.write(byteArray, 0, utfLength);
        return utfLength;
    }

    public final void writeBoolean(final boolean v) throws IOException {
        out.write(v ? 1 : 0);
        bytesCount++;
    }

    public final void writeByte(final int v) throws IOException {
        out.write(v);
        bytesCount++;
    }

    public long getBytesCount() {
        return bytesCount;
    }

}
