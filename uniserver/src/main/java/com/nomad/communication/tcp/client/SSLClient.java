package com.nomad.communication.tcp.client;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nomad.client.ClientInterface;
import com.nomad.client.RawClientInterface;
import com.nomad.exception.SystemException;
import com.nomad.message.CommonAnswer;
import com.nomad.message.CommonMessage;
import com.nomad.model.CommonClientModel;
import com.nomad.server.ServerContext;

public class SSLClient<K extends CommonMessage, T extends CommonAnswer> extends TcpClient<K, T> implements ClientInterface<K, T>, RawClientInterface {

    private static Logger LOGGER = LoggerFactory.getLogger(SSLClient.class);

    public SSLClient(final CommonClientModel clientModel, final ServerContext context) throws SystemException {
        super(clientModel, context);
    }

    private Socket getSocket() throws SystemException {

        String storeName = clientModel.getProperties().get("javax.net.ssl.keyStore");
        String password = clientModel.getProperties().get("javax.net.ssl.keyStorePassword");
        if (password == null) {
            password = "";
        }
        try {
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance("SunX509");
            KeyStore trustKeyStore = KeyStore.getInstance("JKS");
            File trustCertificate = new File(storeName);

            InputStream trustStream = new FileInputStream(trustCertificate);
            trustKeyStore.load(trustStream, password.toCharArray());
            trustStream.close();
            trustManagerFactory.init(trustKeyStore);

            SSLContext context = SSLContext.getInstance("TLS");
            context.init(null, trustManagerFactory.getTrustManagers(), new SecureRandom());
            SSLSocketFactory factory = context.getSocketFactory();

            return factory.createSocket(clientModel.getHost(), clientModel.getPort());
        } catch (NoSuchAlgorithmException | KeyStoreException | IOException | CertificateException | KeyManagementException e) {
            throw new SystemException(e.getMessage(), e);
        }
    }

    @Override
    public void checkConnect() throws UnknownHostException, IOException {
        if (clientModel.getHost() == null) {
            return;
        }
        try {
            if (client == null) {
                client = getSocket();
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

            client = getSocket();
            input = client.getInputStream();
            output = client.getOutputStream();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    @Override
    public InputStream getInput() {
        return input;
    }

    @Override
    public OutputStream getOutput() {
        return output;
    }

}
