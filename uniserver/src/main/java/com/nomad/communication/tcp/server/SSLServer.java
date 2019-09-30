package com.nomad.communication.tcp.server;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.security.KeyStore;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocketFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nomad.communication.MessageExecutorFactory;
import com.nomad.communication.NetworkServer;
import com.nomad.exception.SystemException;
import com.nomad.message.CommonAnswer;
import com.nomad.message.CommonMessage;
import com.nomad.model.CommonServerModel;
import com.nomad.server.ServerContext;
import com.nomad.server.ServerContext.ServiceName;
import com.nomad.server.StoreModelService;

public class SSLServer<T extends CommonMessage, K extends CommonAnswer> extends TcpServer<T, K> implements NetworkServer {

    protected static Logger LOGGER = LoggerFactory.getLogger(SSLServer.class);

    public SSLServer(final CommonServerModel serverModel, final ServerContext context, final String serverType, final MessageExecutorFactory<T, K> workerFactory)
            throws SystemException {
        super(serverModel, context, serverType, workerFactory);
    }

    @Override
    protected void openServerSocket() {
        LOGGER.info("Try to open host:{} port:{}", serverModel.getHost(), serverModel.getPort());
        String storeName = serverModel.getProperties().get("javax.net.ssl.keyStore"); // "keystore.jks"
        String password = serverModel.getProperties().get("javax.net.ssl.keyStorePassword"); // "changeit"
        String keyPassword = serverModel.getProperties().get("javax.net.ssl.keyPassword"); // "changeit"



        try {
            SSLContext sslContext = SSLContext.getInstance("TLS");
            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("SunX509");
            FileInputStream fin = new FileInputStream(storeName);
            KeyStore keyStore = KeyStore.getInstance("JKS");
            keyStore.load(fin, password.toCharArray());
            fin.close();
            keyManagerFactory.init(keyStore, keyPassword.toCharArray());
            sslContext.init(keyManagerFactory.getKeyManagers(), null, null);
            SSLServerSocketFactory sslServerSocketFactory = sslContext.getServerSocketFactory();

            if (serverModel.getHost() != null) {
                final InetAddress address = InetAddress.getByName(serverModel.getHost());
                serverSocket = sslServerSocketFactory.createServerSocket(serverModel.getPort(), 100, address);

            } else {
                serverSocket = sslServerSocketFactory.createServerSocket(serverModel.getPort());
            }
            LOGGER.info(" host:{} port:{}", serverModel.getHost(), serverModel.getPort() + " openned");
        } catch (final IOException e) {
            throw new RuntimeException("Cannot open port: " + serverModel.getPort(), e);
        } catch (final Exception e) {
            throw new RuntimeException("Cannot open port: " + serverModel.getPort(), e);
        }

        if (serverModel.getPort() == 0) {
            serverModel.setPort(serverSocket.getLocalPort());
        }
        final StoreModelService modelService = (StoreModelService) context.get(ServiceName.STORE_MODEL_SERVICE);
        String serverName = "No name tcp server";
        if (modelService != null) {
            serverName = modelService.getServerModel().getServerName();
        }

        context.getInformationPublisherService().publicData(serverInfo, serverName, serverType, serverSocket.getInetAddress().getHostName() + "-" + serverSocket.getLocalPort());

    }

}