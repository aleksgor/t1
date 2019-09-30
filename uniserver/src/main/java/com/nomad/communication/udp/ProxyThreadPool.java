package com.nomad.communication.udp;

import java.net.DatagramSocket;
import java.util.concurrent.atomic.AtomicInteger;

import com.nomad.exception.LogicalException;
import com.nomad.exception.SystemException;
import com.nomad.server.ServerContext;
import com.nomad.utility.pool.ObjectPoolImpl;

public class ProxyThreadPool extends ObjectPoolImpl<UDPPooledProxyThread> {
    private DatagramSocket socket;
    protected AtomicInteger index = new AtomicInteger();

    public void setSocket(DatagramSocket socket) {
        this.socket = socket;

    }

    public ProxyThreadPool(final int poolSize, final long timeout, final int checkDelay, final ServerContext context, final boolean dynamic) {
        super(poolSize, timeout, checkDelay, context, dynamic);
    }

    @Override
    public UDPPooledProxyThread getNewPooledObject() throws SystemException, LogicalException {
        AbstractUDPSenderReceiver senderReceiver = new AbstractUDPSenderReceiver(socket);
        String serverName = socket.getLocalAddress().getHostName() + ":" + socket.getLocalPort();
        return new UDPPooledProxyThread(context, senderReceiver, index.incrementAndGet(), serverName);


    }

    @Override
    public String getPoolId() {

        return "ProxyThreadPool";
    }

}
