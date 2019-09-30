package com.nomad.server;

import java.io.FileInputStream;
import java.net.InetAddress;
import java.security.KeyStore;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocketFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SSLTcpListener extends TCPListener implements ServerListener {

    private static Logger LOGGER = LoggerFactory.getLogger(SSLTcpListener.class);


    @Override
    protected void openServerSocket() {
        try {

            String storename = listener.getProperties().get("javax.net.ssl.keyStore");
            String password = listener.getProperties().get("javax.net.ssl.keyStorePassword");
            String keypassword = listener.getProperties().get("javax.net.ssl.keyPassword");

            SSLContext sslContext = SSLContext.getInstance("TLS");
            KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
            FileInputStream fin;
            try {
                fin = new FileInputStream(storename);
            } catch (Exception e) {
                LOGGER.error("file " + storename + " not found !!");
                throw e;
            }

            KeyStore ks = KeyStore.getInstance("JKS");
            ks.load(fin, password.toCharArray());
            fin.close();
            kmf.init(ks, keypassword.toCharArray());
            sslContext.init(kmf.getKeyManagers(), null, null);
            SSLServerSocketFactory sslServerSocketFactory = sslContext.getServerSocketFactory();


            if (listener.getHost() != null && !"*".equals(listener.getHost())) {
                final InetAddress address = InetAddress.getByName(listener.getHost());
                serverSocket = sslServerSocketFactory.createServerSocket(listener.getPort(), listener.getBacklog(), address);
            } else {
                serverSocket = sslServerSocketFactory.createServerSocket(listener.getPort(), listener.getBacklog());
            }
        } catch (final Exception e) {
            throw new RuntimeException("Cannot open port: " + listener, e);
        }
    }


}
