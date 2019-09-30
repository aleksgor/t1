package com.nomad.io;

import java.beans.PropertyDescriptor;
import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;
import java.io.Serializable;
import java.io.UTFDataFormatException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
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
import com.nomad.server.DataDefinitionService;

public class MessageInputStream extends FilterInputStream {

    private static Logger LOGGER = LoggerFactory.getLogger(MessageInputStream.class);
    private long bytesCount;
    private final DataDefinitionService dataDefinition;

    public MessageInputStream(final InputStream input, final DataDefinitionService dataDefinitionService) {
        super(input);
        dataDefinition = dataDefinitionService;

    }

    /**
     * Read an int value from an input stream
     *
     * @param inputStream
     * @return int value
     * @throws IOException
     */
    public MessageType readType() throws IOException {
        final byte char1 = (byte) read();
        if (char1 == -1) {
            return null;
        }
        return MessageType.values()[char1];

    }

    @Override
    public int read() throws IOException {
        bytesCount++;
        return in.read();
    }

    // ------------for object read/write-------------------

    private Map<Class<?>, MessageType> type2IndexMap;

    {
        type2IndexMap = new HashMap<>();
        type2IndexMap.put(Byte.class, MessageType.TYPE_BYTE);
        type2IndexMap.put(Short.class, MessageType.TYPE_SHORT);
        type2IndexMap.put(Integer.class, MessageType.TYPE_INT);
        type2IndexMap.put(Float.class, MessageType.TYPE_FLOAT);
        type2IndexMap.put(Long.class, MessageType.TYPE_LONG);
        type2IndexMap.put(Double.class, MessageType.TYPE_DOUBLE);
        type2IndexMap.put(BigDecimal.class, MessageType.TYPE_BIG_DECIMAL);
        type2IndexMap.put(Date.class, MessageType.TYPE_DATE_TIME);
        type2IndexMap.put(Boolean.class, MessageType.TYPE_BOOLEAN);
        type2IndexMap.put(String.class, MessageType.TYPE_STRING);
        type2IndexMap.put(byte[].class, MessageType.TYPE_BYTES);
        type2IndexMap.put(List.class, MessageType.TYPE_LIST);
        type2IndexMap.put(Map.class, MessageType.TYPE_MAP);
        type2IndexMap.put(null, MessageType.TYPE_NULL);
        type2IndexMap.put(int[].class, MessageType.TYPE_ARRAY_INT);
        type2IndexMap.put(long[].class, MessageType.TYPE_ARRAY_LONG);
        type2IndexMap.put(String[].class, MessageType.TYPE_ARRAY_STRING);
        type2IndexMap.put(Serializable.class, MessageType.TYPE_SERIALIZABLE);

    }

    public Object readObject() throws IOException, SystemException {
        return readObject(null);
    }

    /**
     * Currently these data types are supported.
     *
     * Integer Float Double BigDecimal Date Time Timestamp Boolean String byte[]
     * List Map
     *
     * @return
     * @throws IOException
     */
    @SuppressWarnings("static-access")
    public Object readObject(Object object) throws IOException, SystemException {
        // read data type from its index value

        final MessageType typeIndex = readType();
        if (typeIndex == null) {
            return null;
        }
        // read real data
        Object objectValue = null;
        switch (typeIndex) {
        case TYPE_NULL:
            break;
        case TYPE_INT:
            objectValue = new Integer(readInteger());
            break;
        case TYPE_SHORT:
            objectValue = new Short(readShort());
            break;
        case TYPE_BYTE:
            objectValue = new Byte(readByte());
            break;
        case TYPE_LONG:
            objectValue = new Long(readLong());
            break;
        case TYPE_FLOAT:
            objectValue = new Float(readFloat());
            break;
        case TYPE_DOUBLE:
            objectValue = new Double(readDouble());
            break;
        case TYPE_BIG_DECIMAL:
            objectValue = new BigDecimal(readUTF());
            break;
        case TYPE_DATE_TIME:
            objectValue = new Date(readLong());
            break;
        case TYPE_BOOLEAN:
            objectValue = new Boolean(readBoolean());
            break;
        case TYPE_STRING:
            objectValue = readUTF();
            break;
        case TYPE_BYTES:
            int length = readInteger();
            byte[] bytes = new byte[length];
            if (length > 0) {
                readFully(bytes);
                bytesCount += bytes.length;
            }
            objectValue = bytes;
            break;
        case TYPE_LIST:
            objectValue = readList();
            break;
        case TYPE_MAP:
            objectValue = readMap();
            break;
        case TYPE_ARRAY_INT:
            objectValue = readArrayInteger();
            break;
        case TYPE_ARRAY_LONG:
            objectValue = readArrayLong();
            break;
        case TYPE_ARRAY_STRING:
            objectValue = readArrayString();
            break;
        case TYPE_SERIALIZABLE:
            length = readInteger();

            if (length != 0) {
                bytes = new byte[length];
                readFully(bytes);

                try {
                    final ObjectInputStream objectInputStream = new ObjectInputStream(new ByteArrayInputStream(bytes)) {
                        @Override
                        protected Class<?> resolveClass(final ObjectStreamClass desc) throws IOException, ClassNotFoundException {

                            return Class.forName(desc.getName());
                        }
                    };
                    objectValue = objectInputStream.readObject();

                    LOGGER.warn("native serializaion: " + objectValue.getClass().getName());

                } catch (final Exception e) {
                    LOGGER.error(e.getMessage(), e);
                    throw new IOException(e.getMessage());
                }
            }
            break;
        case TYPE_MANUAL_SERIALIZABLE:
            final String clazz = readUTF();
            final SerializerPooledObject<Object> serializer = SerializerFactory.getSerializer(clazz);
            if (serializer == null) {
                LOGGER.error("cannot find serializer for class:" + clazz);
                throw new RuntimeException();
            }
            objectValue = serializer.getSerializer().read(this);
            serializer.freeObject();
            break;
        case TYPE_MODEL:
            objectValue = readModel();
            break;
        case TYPE_IDENTIFIER:
            objectValue = readIdentifier();
            break;
        case TYPE_ENUM:
            final String value = readUTF();

            if (object == null) {
                LOGGER.warn("Parameter cannot be null");
                objectValue = value;
            } else {
                Enum<?> e = (Enum<?>) object;
                objectValue = e.valueOf(e.getDeclaringClass(), value);
            }
            break;
        }
        return objectValue;
    }

    private Object readArrayInteger() throws IOException {
        // check null
        if (MessageType.TYPE_NULL.equals(readType()))
            return null;
        // read map size
        final int size = readInteger();
        if (size == 0) {
            return new int[0];
        }
        final int[] data = new int[size];
        // write real data
        for (int i = 0; i < size; i++) {
            data[i] = readInteger();
        }
        return data;
    }

    private Object readArrayLong() throws IOException {
        // check null
        if (MessageType.TYPE_NULL.equals(readType()))
            return null;
        // read map size
        final int size = readInteger();
        if (size == 0) {
            return new int[0];
        }
        final long[] data = new long[size];
        // write real data
        for (int i = 0; i < size; i++) {
            data[i] = readLong();
        }
        return data;
    }

    private Object readArrayString() throws IOException {
        // check null
        if (MessageType.TYPE_NULL.equals(readType()))
            return null;
        // read map size
        final int size = readInteger();
        if (size == 0) {
            return new int[0];
        }
        final String[] data = new String[size];
        // write real data
        for (int i = 0; i < size; i++) {
            data[i] = readString();
        }
        return data;
    }

    /**
     * Read a String from an input stream
     *
     * @param inputStream
     * @return an String
     * @throws IOException
     */
    public String readString() throws IOException {
        final MessageType type = readType();
        if (type == null) {
            return null;
        }
        if (MessageType.TYPE_NULL.equals(type)) {
            return null;
        } else {
            return readUTF();
        }
    }

    /**
     * Read a list from an input stream
     *
     * @param dos
     * @return
     * @throws IOException
     * @throws BirtException
     */
    public byte[] readBytes() throws IOException {
        // check null
        if (MessageType.TYPE_NULL.equals(readType()))
            return null;

        // read bytes size
        final int size = readInteger();
        final byte[] bytes = new byte[size];
        if (size != 0) {
            readFully(bytes);
        }
        return bytes;
    }

    /**
     * Read a list from an input stream
     *
     */
    public Collection<?> readList() throws IOException, SystemException {
        // check null
        if (MessageType.TYPE_NULL.equals(readType()))
            return null;
        // read map size
        final List<Object> dataList = new ArrayList<>();
        final int size = readInteger();
        if (size == 0) {
            return dataList;
        }
        // write real data
        for (int i = 0; i < size; i++) {
            dataList.add(readObject());
        }
        return dataList;
    }

    /**
     * Read a list from an input stream
     */
    @SuppressWarnings("unchecked")
    public <T extends Object> Collection<T> readList(Collection<T> dataList) throws IOException, SystemException {
        // check null
        if (MessageType.TYPE_NULL.equals(readType()))
            return null;
        // read map size
        final int size = readInteger();
        if (size == 0) {
            return dataList;
        }
        // write real data
        for (int i = 0; i < size; i++) {
            T data= (T) readObject();
            dataList.add(data);
        }
        return dataList;
    }

    /**
     * Read a Map from an input stream
     */
    public Map<? extends Object, ? extends Object> readMap() throws IOException, SystemException {
        // check null
        if (MessageType.TYPE_NULL.equals(readType()))
            return null;

        // read map size
        final Map<Object, Object> dataMap = new HashMap<>();
        final int size = readInteger();
        if (size == 0)
            return dataMap;

        // write real data
        for (int i = 0; i < size; i++) {
            final Object key = readObject();
            final Object value = readObject();
            dataMap.put(key, value);
        }

        return dataMap;
    }

    /**
     * private utility method to read a UTF String
     *
     * @param str
     * @throws UTFDataFormatException
     */
    private String readUTF() throws IOException {
        final int length = readInteger();
        final byte[] result = new byte[length];
        int read = 0;
        while (length > read) {
            int i = read(result, read, length - read);
            read += i;
        }

        return convertBytes2String(result);
    }

    /**
     * private utility method helping to convert byte[] to a String
     *
     * @throws UTFDataFormatException
     */
    private int generateCharArray(final char[] charArray, final byte[] byteArray, int count, int charArrayCount) throws UTFDataFormatException {
        int c, char2, char3;
        final int utfLength = byteArray.length;
        while (count < utfLength) {
            c = byteArray[count] & 0xff;
            switch (c >> 4) {
            case 0:
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
            case 7:
                // 0xxxxxxx
                count++;
                charArray[charArrayCount++] = (char) c;
                break;
            case 12:
            case 13:
                // 110x xxxx 10xx xxxx
                count += 2;
                if (count > utfLength)
                    throw new UTFDataFormatException("Malformed input: partial character at end");
                char2 = byteArray[count - 1];
                if ((char2 & 0xC0) != 0x80)
                    throw new UTFDataFormatException("Malformed input around byte " + count);
                charArray[charArrayCount++] = (char) (((c & 0x1F) << 6) | (char2 & 0x3F));
                break;
            case 14:
                // 1110 xxxx 10xx xxxx 10xx xxxx
                count += 3;
                if (count > utfLength)
                    throw new UTFDataFormatException("Malformed input: partial character at end");
                char2 = byteArray[count - 2];
                char3 = byteArray[count - 1];
                if (((char2 & 0xC0) != 0x80) || ((char3 & 0xC0) != 0x80))
                    throw new UTFDataFormatException("Malformed input around byte " + (count - 1));
                charArray[charArrayCount++] = (char) (((c & 0x0F) << 12) | ((char2 & 0x3F) << 6) | ((char3 & 0x3F) << 0));
                break;
            default:
                // 10xx xxxx, 1111 xxxx
                throw new UTFDataFormatException("Malformed input around byte " + count);
            }
        }
        return charArrayCount;
    }

    /**
     * private utility method to convert a byte[] to String
     *
     * @param bytearre
     * @throws UTFDataFormatException
     */
    private String convertBytes2String(final byte[] byteArray) throws UTFDataFormatException {
        final int utfLength = byteArray.length;
        final char[] charArray = new char[utfLength];
        int c;
        int charArrayCount = 0;

        int count = 0;
        while (count < utfLength) {
            c = byteArray[count] & 0xff;
            if (c > 127)
                break;
            count++;
            charArray[charArrayCount++] = (char) c;
        }
        charArrayCount = generateCharArray(charArray, byteArray, count, charArrayCount);

        // The number of chars produced may be less than utflen
        bytesCount += charArrayCount;
        return new String(charArray, 0, charArrayCount);
    }

    public int readInteger() throws IOException {
        final int char1 = in.read();
        final int char2 = in.read();
        final int char3 = in.read();
        final int char4 = in.read();
        if ((char1 | char2 | char3 | char4) < 0) {
            throw new EOFException();
        }
        bytesCount += 4;
        return ((char1 << 24) + (char2 << 16) + (char3 << 8) + (char4 << 0));
    }

    public short readShort() throws IOException {
        final int char1 = in.read();
        final int char2 = in.read();
        if ((char1 | char2) < 0) {
            throw new EOFException();
        }
        bytesCount += 2;
        return (short) ((char1 << 8) + (char2 << 0));
    }

    public byte readByte() throws IOException {
        final int char1 = in.read();
        if (char1 < 0) {
            throw new EOFException();
        }
        bytesCount += 1;
        return (byte) char1;
    }

    private final byte readBuffer[] = new byte[8];

    public final long readLong() throws IOException {
        readFully(readBuffer, 0, 8);
        bytesCount += 8;

        return (((long) readBuffer[0] << 56) + ((long) (readBuffer[1] & 255) << 48) + ((long) (readBuffer[2] & 255) << 40)
                + ((long) (readBuffer[3] & 255) << 32) + ((long) (readBuffer[4] & 255) << 24) + ((readBuffer[5] & 255) << 16) + ((readBuffer[6] & 255) << 8) + ((readBuffer[7] & 255) << 0));
    }

    public final void readFully(final byte b[]) throws IOException {
        readFully(b, 0, b.length);

    }

    public final void readFully(final byte b[], final int off, final int length) throws IOException {
        if (length < 0)
            throw new IndexOutOfBoundsException();
        int n = 0;
        while (n < length) {
            final int count = in.read(b, off + n, length - n);
            if (count < 0)
                throw new EOFException();
            n += count;
        }
        bytesCount += length;
    }

    public final float readFloat() throws IOException {
        return Float.intBitsToFloat(readInteger());
    }

    public final double readDouble() throws IOException {
        return Double.longBitsToDouble(readLong());
    }

    /*
     * public final String readUTF() throws IOException { int utflen = readInt(); byte[] bytearr = null; char[] chararr = null; bytearr = new byte[utflen];
     * chararr = new char[utflen]; int c, char2, char3; int count = 0; int chararr_count = 0; readFully(bytearr, 0, utflen); while (count < utflen) { c = (int)
     * bytearr[count] & 0xff; if (c > 127) break; count++; chararr[chararr_count++] = (char) c; } while (count < utflen) { c = (int) bytearr[count] & 0xff;
     * switch (c >> 4) { case 0: case 1: case 2: case 3: case 4: case 5: case 6: case 7: // 0xxxxxxx count++; chararr[chararr_count++] = (char) c; break; case
     * 12: case 13: // 110x xxxx 10xx xxxx count += 2; if (count > utflen) throw new UTFDataFormatException("malformed input: partial character at end"); char2
     * = (int) bytearr[count - 1]; if ((char2 & 0xC0) != 0x80) throw new UTFDataFormatException("malformed input around byte " + count);
     * chararr[chararr_count++] = (char) (((c & 0x1F) << 6) | (char2 & 0x3F)); break; case 14: // 1110 xxxx 10xx xxxx 10xx xxxx count += 3; if (count > utflen)
     * throw new UTFDataFormatException("malformed input: partial character at end"); char2 = (int) bytearr[count - 2]; char3 = (int) bytearr[count - 1]; if
     * (((char2 & 0xC0) != 0x80) || ((char3 & 0xC0) != 0x80)) throw new UTFDataFormatException("malformed input around byte " + (count - 1));
     * chararr[chararr_count++] = (char) (((c & 0x0F) << 12) | ((char2 & 0x3F) << 6) | ((char3 & 0x3F) << 0)); break; default: // 10xx xxxx, 1111 xxxx throw new
     * UTFDataFormatException("malformed input around byte " + count); } } // The number of chars produced may be less than utflen return new String(chararr, 0,
     * chararr_count); }
     */
    public final int readUnsignedShort() throws IOException {
        final int char1 = in.read();
        final int char2 = in.read();
        bytesCount += 2;
        if ((char1 | char2) < 0)
            throw new EOFException();
        return (char1 << 8) + (char2 << 0);
    }

    public long getBytesCount() {
        return bytesCount;
    }

    public boolean readBoolean() throws IOException {
        final int character = in.read();
        bytesCount += 1;
        if (character < 0)
            throw new EOFException();
        return (character != 0);
    }

    public Model readModel() throws SystemException {
        try {
            final String modelName = readUTF();

            final ModelDescription description = dataDefinition.getModelDescription(modelName);
            if (description == null) {
                LOGGER.equals("model:" + modelName + " does not supported!");
                throw new SystemException("model:" + modelName + " does not supported!");
            }
            @SuppressWarnings("unchecked")
            final Class<Model> modelClazz = (Class<Model>) Class.forName(description.getClazz());
            final Model model = modelClazz.newInstance();

            final int idType = in.read();
            if (idType == MessageType.TYPE_IDENTIFIER.getCode()) {
                @SuppressWarnings("unchecked")
                final Class<Identifier> clazzId = (Class<Identifier>) Class.forName(description.getClassId());
                final Identifier id = clazzId.newInstance();
                for (final Field field : description.getPrimaryKeyFields()) {
                    final Object data = readObject();
                    new PropertyDescriptor(field.getName(), clazzId).getWriteMethod().invoke(id, data);
                }

                model.setIdentifier(id);
            }
            for (final Field field : description.getFields().values()) {
                final Object data = readObject();
                new PropertyDescriptor(field.getName(), modelClazz).getWriteMethod().invoke(model, data);
            }
            // relations
            for (final Relation relation : description.getRelations().values()) {
                final Object data = readObject();
                new PropertyDescriptor(relation.getFieldName(), modelClazz).getWriteMethod().invoke(model, data);
            }

            return model;
        } catch (final Exception e) {
            throw new SystemException(e.getMessage(), e);
        }
    }

    public Identifier readIdentifier() throws SystemException {
        try {
            final String modelName = readUTF();

            final ModelDescription description = dataDefinition.getModelDescription(modelName);
            if (description == null) {
                LOGGER.equals("model:" + modelName + " does not supported!");
                throw new SystemException("model:" + modelName + " does not supported!");
            }

            @SuppressWarnings("unchecked")
            final Class<Identifier> clazzId = (Class<Identifier>) Class.forName(description.getClassId());
            final Identifier id = clazzId.newInstance();
            for (final Field field : description.getPrimaryKeyFields()) {
                final Object data = readObject();
                new PropertyDescriptor(field.getName(), clazzId).getWriteMethod().invoke(id, data);
            }

            return id;
        } catch (final Exception e) {
            throw new SystemException(e.getMessage(), e);
        }
    }
}
