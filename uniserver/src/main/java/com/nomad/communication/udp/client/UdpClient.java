package com.nomad.communication.udp.client;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nomad.client.ClientInterface;
import com.nomad.client.RawClientInterface;
import com.nomad.communication.udp.AbstractUDPSenderReceiver;
import com.nomad.communication.udp.UdpMessageImpl;
import com.nomad.exception.SystemException;
import com.nomad.message.CommonAnswer;
import com.nomad.message.CommonMessage;
import com.nomad.message.MessageSenderReceiver;
import com.nomad.message.MessageSenderReceiverImpl;
import com.nomad.message.RawMessage;
import com.nomad.model.CommonClientModel;
import com.nomad.server.DataDefinitionService;
import com.nomad.server.ServerContext;

public class UdpClient<K extends CommonMessage, T extends CommonAnswer> implements ClientInterface<K, T>, RawClientInterface {
    @SuppressWarnings("unused")
    private static Logger LOGGER = LoggerFactory.getLogger(UdpClient.class);

    private final int port;
    private final InetAddress address;
    private final DatagramSocket socket;
    private final AbstractUDPSenderReceiver udpSR;
    private long size = 0;
    protected final ServerContext context;
    private final MessageSenderReceiver msr;
    protected static String serverName;
    protected final CommonClientModel clientModel;

    public UdpClient(final CommonClientModel clientModel, final ServerContext context) throws SystemException {

        this.context = context;
        DataDefinitionService dataDefinitionService = null;
        dataDefinitionService = context.getDataDefinitionService(null);
        msr = new MessageSenderReceiverImpl((byte) 0x1, dataDefinitionService);
        try {
            socket = new DatagramSocket();
            udpSR = new AbstractUDPSenderReceiver(socket);
            port = clientModel.getPort();
            address = InetAddress.getByName(clientModel.getHost());
        } catch (SocketException | UnknownHostException e) {
            throw new SystemException(e);
        }
        this.clientModel = clientModel;

    }

    @SuppressWarnings("unchecked")
    @Override
    public T sendMessage(final K message) throws SystemException {
        try {

            msr.reset();
            final ByteArrayOutputStream output = new ByteArrayOutputStream();
            msr.storeObject(message, output);
            // send
            final UdpMessageImpl innerMessage = new UdpMessageImpl();
            innerMessage.setLength(output.toByteArray().length);
            innerMessage.setData(output.toByteArray());
            innerMessage.setPort(port);
            innerMessage.setAddress(address);
            udpSR.send(innerMessage);

            // Receive
            final byte[] data = udpSR.receiveByte();

            final InputStream input = new ByteArrayInputStream(data);
            final T result = (T) msr.getObject(input);
            size += msr.getInBytes() + msr.getOutBytes();
            return result;
        } catch (final Exception e) {
            throw new SystemException(e);
        }

    }

    @Override
    public RawMessage sendRawMessage(RawMessage message) throws SystemException {
        try {
            msr.reset();
            final ByteArrayOutputStream output = new ByteArrayOutputStream();
            msr.assembleRawMessage(message, output);
            // send
            final UdpMessageImpl innerMessage = new UdpMessageImpl();
            innerMessage.setLength(output.toByteArray().length);
            innerMessage.setData(output.toByteArray());
            innerMessage.setPort(port);
            innerMessage.setAddress(address);
            udpSR.send(innerMessage);

            // Receive
            final byte[] data = udpSR.receiveByte();

            final InputStream input = new ByteArrayInputStream(data);
            final RawMessage result = msr.getRawMessage(input);
            size += msr.getInBytes() + msr.getOutBytes();
            return result;
        } catch (Exception e) {
            throw new SystemException(e);
        }
    }

    @Override
    public void close() {
        udpSR.close();
    }

    public int checkClean() {
        return udpSR.checkClean();
    }

    @Override
    public long getMessageSize() {
        long a = size;
        size = 0;
        return a;
    }
}
