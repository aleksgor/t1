package com.nomad.communication.tcp.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nomad.client.ClientInterface;
import com.nomad.client.RawClientInterface;
import com.nomad.exception.SystemException;
import com.nomad.message.CommonAnswer;
import com.nomad.message.CommonMessage;
import com.nomad.message.MessageSenderReceiver;
import com.nomad.message.MessageSenderReceiverImpl;
import com.nomad.message.RawMessage;
import com.nomad.model.CommonClientModel;
import com.nomad.server.DataDefinitionService;
import com.nomad.server.ServerContext;
import com.nomad.server.ServerContext.ServiceName;
import com.nomad.server.StoreModelService;

public  class TcpClient<K extends CommonMessage, T extends CommonAnswer>  implements ClientInterface<K, T>, RawClientInterface {

    protected Socket client = null;
    protected InputStream input;
    protected OutputStream output;
    private static Logger LOGGER = LoggerFactory.getLogger(TcpClient.class);
    private long size = 0;
    protected final ServerContext context;
    private final MessageSenderReceiver msr;
    protected static String serverName;
    protected final CommonClientModel clientModel;

    public TcpClient(final CommonClientModel clientModel, final ServerContext context) throws SystemException {

        this.context = context;
        this.clientModel = clientModel;

        DataDefinitionService dataDefinitionService;
        try {
            dataDefinitionService = context.getDataDefinitionService(null);
        } catch (Exception e) {
          throw new SystemException(e);
        }
        msr = new MessageSenderReceiverImpl((byte) 0x1, dataDefinitionService);

        final StoreModelService storeModelService = (StoreModelService) context.get(ServiceName.STORE_MODEL_SERVICE);
        if (storeModelService != null) {
            serverName = storeModelService.getServerModel().getServerName();
        } else {
            serverName = clientModel.getHost() + ":" + clientModel.getPort();
        }
    }


    @Override
    @SuppressWarnings("unchecked")
    public T sendMessage(final K message) throws SystemException {
        try {
            msr.reset();
            checkConnect();
            msr.storeObject(message, output);
            final T result = (T) msr.getObject(input);
            size = msr.getInBytes() + msr.getOutBytes();
            return result;
        }catch(final IOException e){
            throw new SystemException(e);
        } catch (final Throwable e) {
            LOGGER.error("host:" + clientModel.getHost() + " port:" + clientModel.getPort() + " message:" + message + e.getMessage());
            throw new  SystemException(e);
        }

    }
    @Override
    public RawMessage sendRawMessage(RawMessage message) throws SystemException {
        try {
            msr.reset();
            checkConnect();
            msr.assembleRawMessage(message, output);
            final RawMessage result =  msr.getRawMessage(input);
            size = msr.getInBytes() + msr.getOutBytes();
            return result;
        }catch(final IOException e){
            throw new SystemException(e);
        } catch (final Exception e) {
            LOGGER.error("serverName:" + serverName + " host:" + clientModel.getHost() + " port:" + clientModel.getPort() + " message:" + message + e.getMessage());
            throw new  SystemException(e);
        }

    }

    public void checkConnect() throws UnknownHostException, IOException {
        if (clientModel.getHost() == null) {
            return;
        }
        if (client == null) {
            client = new Socket(clientModel.getHost(), clientModel.getPort());
            input = client.getInputStream();
            output = client.getOutputStream();
            return;
        }
        if (client.isConnected()) {
            return;
        }
        // try to reconnect
        try {
            client.close();
        } catch (final Throwable t) {

        }

        client = new Socket(clientModel.getHost(), clientModel.getPort());
        input = client.getInputStream();
        output = client.getOutputStream();

    }

    public InputStream getInput() {
        return input;
    }

    public OutputStream getOutput() {
        return output;
    }

    @Override
    public long getMessageSize() {
        long a=size;
        size=0;
        return a;
    }

    @Override
    public void close() {
        try {
            if (input != null) {
                input.close();
            }
            if (output != null) {
                output.close();
            }
            if (client != null) {

                client.close();
            }
        } catch (final IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }


}
