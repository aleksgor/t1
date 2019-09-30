package com.nomad.communication.udp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import com.nomad.utility.ByteUtility;

public class  AbstractUDPSenderReceiver   {
    private final DatagramSocket socket;
    protected final byte[] CLIENT_ID;
    private final int PACKET_SIZE = (15 * 1024) - ByteUtility.HEADER_LENGTH;
    private static final AtomicInteger messageId = new AtomicInteger();
    private volatile Map<Header, Map<Integer, byte[]>> packetBuffer = new HashMap<>();
    private final Map<Integer, AddressPort> addressStore = new ConcurrentHashMap<>();

    public AbstractUDPSenderReceiver(final DatagramSocket socket  ) {
        this.socket=socket;
        CLIENT_ID = ByteUtility.writeInt(UUID.randomUUID().hashCode());
    }


    public void send(final UdpMessageImpl message) throws IOException {
        final int i = messageId.incrementAndGet();
        final byte[] bytes = ByteUtility.writeInt(i);

        boolean finish = false;
        int packetNumber = 0;
        int startByte = 0;
        byte[] header;
        byte[] data;
        while (!finish) {
            final int messageChunkSize = Math.min((message.getLength() - startByte), PACKET_SIZE - ByteUtility.HEADER_LENGTH);
            byte lastPacket = 0;
            if (message.getLength() <= (startByte + messageChunkSize)) {
                lastPacket = packetNumber == 0 ? ByteUtility.ALONE_PACKET : ByteUtility.LAST_PACKET;
            }
            header = getHeader(messageChunkSize, bytes, ByteUtility.writeInt(packetNumber), lastPacket);

            data = message.getChunk(startByte, messageChunkSize);
            socket.send(new DatagramPacket(ByteUtility.concatenate(header, data), data.length + header.length, message.getAddress(), message.getPort()));
            if (lastPacket != 0) {
                finish = true;
            }
            packetNumber++;
            startByte += messageChunkSize;
        }
    }

    // 4b - packet length 4b - client Id 4b - message Id; 4b-packet number
    // (0-start)
    private byte[] getHeader(final int packetLength, final byte[] messageId, final byte[] packetNumber, final byte last) {
        final byte[] header = new byte[ByteUtility.HEADER_LENGTH];
        final byte[] tempArray = ByteUtility.writeInt(packetLength);
        header[0] = tempArray[0];
        header[1] = tempArray[1];
        header[2] = tempArray[2];
        header[3] = tempArray[3];

        header[4] = CLIENT_ID[0];
        header[5] = CLIENT_ID[1];
        header[6] = CLIENT_ID[2];
        header[7] = CLIENT_ID[3];

        header[8] = messageId[0];
        header[9] = messageId[1];
        header[10] = messageId[2];
        header[11] = messageId[3];

        header[12] = packetNumber[0];
        header[13] = packetNumber[1];
        header[14] = packetNumber[2];
        header[15] = packetNumber[3];
        header[16] = last;
        return header;
    }

    private Header parseHeader(final byte[] data) {
        if (data.length < ByteUtility.HEADER_LENGTH) {
            throw new IllegalArgumentException("Input array must have 17 bytes:" + data.length);
        }
        final Header result = new Header();
        result.packetLength = ByteUtility.copyInteger(data, 0);
        result.clientId = ByteUtility.copyInteger(data, 4);
        result.messageId = ByteUtility.copyInteger(data, 8);
        result.packetNumber = ByteUtility.copyInteger(data, 12);
        result.lastPacket = data[16];
        return result;

    }

    private class Header {
        private int clientId;
        private int packetLength;
        private int messageId;
        private int packetNumber;
        private byte lastPacket;

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + clientId;
            result = prime * result + messageId;
            return result;
        }

        @Override
        public boolean equals(final Object obj) {
            final Header other = (Header) obj;
            if (clientId != other.clientId)
                return false;
            if (messageId != other.messageId)
                return false;
            return true;
        }

        @Override
        public String toString() {
            return "Header [clentId=" + clientId + ", packetLength=" + packetLength + ", messageId=" + messageId + ", packetNumber=" + packetNumber + ", lastPacket=" + lastPacket
                    + "]";
        }

    }

    private UdpMessageImpl completeData(final Header header) {

        final Map<Integer, byte[]> messageBuffer = packetBuffer.remove(header);
        if (messageBuffer == null) {
            throw new IllegalArgumentException("Unknown buffer:" + header);
        }
        if (messageBuffer.size() == 1) {
            final byte[] chunk = messageBuffer.values().iterator().next();
            final Header header1 = parseHeader(chunk);
            if (header1.lastPacket == ByteUtility.ALONE_PACKET) {
                final UdpMessageImpl result = new UdpMessageImpl(ByteUtility.getBody(chunk, header.packetLength));
                final AddressPort addressPort = addressStore.remove(header.messageId);
                result.setAddress(addressPort.address);
                result.setPort(addressPort.port);
                return result;
            }
        }

        // final byte[] result = new byte[0];
        final UdpMessageImpl result = new UdpMessageImpl();

        for (int i = 0; i < Integer.MAX_VALUE; i++) {
            final byte[] chunk = messageBuffer.get(i);
            if (chunk != null) {
                final Header header1 = parseHeader(chunk);
                result.addChunk(ByteUtility.getBody(chunk, header1.packetLength));
                if (header1.lastPacket != 0) {
                    final AddressPort addressPort = addressStore.remove(header.messageId);
                    result.setAddress(addressPort.address);
                    result.setPort(addressPort.port);

                    return result;
                }
            } else {
                i = Integer.MAX_VALUE;
            }
        }
        packetBuffer.put(header, messageBuffer);
        return null;
    }

    private boolean checkData(final Header header) {

        final Map<Integer, byte[]> messageBuffer = packetBuffer.get(header);
        if (messageBuffer == null) {
            throw new IllegalArgumentException("Unknown buffer:" + header);
        }
        if (messageBuffer.size() == 1) {
            final byte[] chunk = messageBuffer.values().iterator().next();
            final Header header1 = parseHeader(chunk);
            if (header1.lastPacket == ByteUtility.ALONE_PACKET) {
                return true;
            }
        }

        for (int i = 0; i < Integer.MAX_VALUE; i++) {
            final byte[] chunk = messageBuffer.get(i);
            if (chunk != null) {
                final Header header1 = parseHeader(chunk);
                if (header1.lastPacket != 0) {
                    return true;
                }
            } else {
                return false;
            }
        }
        return false;
    }

    public byte[] receiveByte() throws IOException {

        UdpMessageImpl message;
        while ((message = receive()) == null) {
            ;
        }
        return message.getData();

    }

    public UdpMessageImpl receive() throws IOException {

        final DatagramPacket packet = new DatagramPacket(new byte[PACKET_SIZE], PACKET_SIZE);
        socket.receive(packet);
        final Header header = parseHeader(packet.getData());

        if (header.lastPacket == ByteUtility.ALONE_PACKET) {
            final UdpMessageImpl result = new UdpMessageImpl();
            result.setAddress(packet.getAddress());
            result.setPort(packet.getPort());
            result.setLength(header.packetLength);
            final byte[] byteResult = new byte[header.packetLength];
            System.arraycopy(packet.getData(), ByteUtility.HEADER_LENGTH, byteResult, 0, header.packetLength);
            result.setData(byteResult);
            return result;
        }
        addressStore.put(header.messageId, new AddressPort(packet.getAddress(), packet.getPort()));
        Map<Integer, byte[]> messageMap = packetBuffer.get(header);

        if (messageMap == null) {
            messageMap = new HashMap<>();
            packetBuffer.put(header, messageMap);
        }
        messageMap.put(header.packetNumber, packet.getData());
        if (checkData(header)) {
            final UdpMessageImpl result = completeData(header);
            return result;
        }

        return null;
    }

    public int checkClean() {
        return packetBuffer.size() + addressStore.size();
    }

    public void close() {
        socket.close();
    }

    private class AddressPort {
        InetAddress address;
        int port;

        public AddressPort(final InetAddress address, final int port) {
            super();
            this.address = address;
            this.port = port;
        }

    }
}
