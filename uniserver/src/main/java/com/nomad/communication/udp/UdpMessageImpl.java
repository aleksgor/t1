package com.nomad.communication.udp;

import java.net.InetAddress;
import java.util.Arrays;

import com.nomad.utility.ByteUtility;

public class UdpMessageImpl {
    // without header
    private int length;
    // without header
    private byte[] data;
    private InetAddress address;
    private int port;

    public UdpMessageImpl() {
        length = 0;
        data = null;
    }

    public void clean() {

    }

    public UdpMessageImpl(final byte[] data) {
        addChunk(data);
    }

    public int getLength() {
        return length;
    }

    public void setLength(final int length) {
        this.length = length;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(final byte[] data) {
        this.data = data;
        length = data.length;
    }

    public byte[] getChunk(final int offSet, final int length) {
        final byte[] result = new byte[length];
        System.arraycopy(data, offSet, result, 0, Math.min(data.length - offSet, length));
        return result;
    }

    public void addChunk(final byte[] chunk) {
        if (data != null) {
            data = ByteUtility.concatenate(data, chunk);
        } else {
            data = new byte[chunk.length];
            System.arraycopy(chunk, 0, data, 0, chunk.length);
        }
        length = data.length;
    }

    public InetAddress getAddress() {
        return address;
    }

    public void setAddress(final InetAddress address) {
        this.address = address;
    }

    public int getPort() {
        return port;
    }

    public void setPort(final int port) {
        this.port = port;
    }

    @Override
    public String toString() {
        return "UdpMessageImpl [length=" + length + ", data=" + Arrays.toString(data) + ", address=" + address + ", port=" + port + "]";
    }

}
