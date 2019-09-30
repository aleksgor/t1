package com.nomad.utility;


public class ByteUtility {
    public final static byte LAST_PACKET = 2;
    public final static byte ALONE_PACKET=1;
    public final static byte MIDDLE_PACKET = 0;
    public final static int HEADER_LENGTH=17;
    public static byte[] concatenate(final byte[] a, final byte[] b) {

        final byte[] c = new byte[a.length + b.length];
        System.arraycopy(a, 0, c, 0, a.length);
        System.arraycopy(b, 0, c, a.length, b.length);

        return c;
    }

    public static byte[] writeLong(final long v) {
        final byte writeBuffer[] = new byte[8];
        writeBuffer[0] = (byte) (v >>> 56);
        writeBuffer[1] = (byte) (v >>> 48);
        writeBuffer[2] = (byte) (v >>> 40);
        writeBuffer[3] = (byte) (v >>> 32);
        writeBuffer[4] = (byte) (v >>> 24);
        writeBuffer[5] = (byte) (v >>> 16);
        writeBuffer[6] = (byte) (v >>> 8);
        writeBuffer[7] = (byte) (v >>> 0);
        return writeBuffer;
    }

    public static long readLong(final byte[] input) {
        if (input.length != 8) {
            throw new IllegalArgumentException("Input array must have 8 bytes:" + input.length);
        }
        return (((long) input[0] << 56) + ((long) (input[1] & 255) << 48) + ((long) (input[2] & 255) << 40) + ((long) (input[3] & 255) << 32) + ((long) (input[4] & 255) << 24)
                + ((input[5] & 255) << 16) + ((input[6] & 255) << 8) + ((input[7] & 255) << 0));
    }

    public static byte[] writeInt(final int v)  {
        final byte result[] = new byte[4];
        result[0] = (byte) ((v >>> 24) & 0xFF);
        result[1] = (byte) ((v >>> 16) & 0xFF);
        result[2] = (byte) ((v >>> 8) & 0xFF);
        result[3] = (byte) ((v >>> 0) & 0xFF);
        return result;
    }

    public static int readInt(final byte[] input) {
        if (input.length != 4) {
            throw new IllegalArgumentException("Input array must have 8 bytes:" + input.length);
        }
        return (((input[0] & 255) << 24) +
                ((input[1] & 255) << 16) +
                ((input[2] & 255) << 8) +
                ((input[3] & 255) << 0));
    }

    public static int copyInteger(final byte[] original, final int from) {
        final byte[] copy = new byte[4];
        System.arraycopy(original, from, copy, 0, 4);
        return readInt(copy);
    }
    public static byte[] getBody(final byte[] original, final int length) {

        final byte[] copy = new byte[length];
        System.arraycopy(original, HEADER_LENGTH, copy, 0, length);
        return copy;
    }

}
