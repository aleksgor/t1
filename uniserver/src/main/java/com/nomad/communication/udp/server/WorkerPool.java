package com.nomad.communication.udp.server;

import java.net.DatagramSocket;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nomad.communication.MessageExecutor;
import com.nomad.communication.MessageExecutorFactory;
import com.nomad.communication.udp.AbstractUDPSenderReceiver;
import com.nomad.exception.SystemException;
import com.nomad.message.CommonAnswer;
import com.nomad.message.CommonMessage;
import com.nomad.server.ServerContext;
import com.nomad.utility.pool.ObjectPoolImpl;

public class WorkerPool<K extends CommonMessage, T extends CommonAnswer> extends ObjectPoolImpl<UdpWorker<K, T>> {
    protected static Logger LOGGER = LoggerFactory.getLogger(WorkerPool.class);

    private final ServerContext context;
    private final MessageExecutor<K, T> executor;
    protected AtomicInteger index = new AtomicInteger();
    private DatagramSocket socket;

    protected WorkerPool(final int poolSize, final long timeout, final int checkDelay, final ServerContext context, final boolean dynamic,
            final MessageExecutorFactory<K, T> workerFactory, final UdpServer<K, T> server) throws SystemException {
        super(poolSize, timeout, checkDelay, context, dynamic);
        this.context = context;
        executor = workerFactory.getMessageExecutor(context, 0, server);

    }

    public void setSocket(DatagramSocket socket) {
        this.socket = socket;
    }
    @Override
    public UdpWorker<K, T> getNewPooledObject() throws SystemException {
        final AbstractUDPSenderReceiver senderReceiver = new AbstractUDPSenderReceiver(socket);
        final UdpWorker<K, T> worker = new UdpWorker<>(context, senderReceiver, index.incrementAndGet(), executor);

        return worker;
    }

    /*
     * public UdpWorker<K, T> getNewPooledObject(final int index, final DatagramSocket socket, final UdpMessageImpl data) throws Exception { final
     * AbstractUDPSenderReceiver senderReceiver= new AbstractUDPSenderReceiver(socket); final UdpWorker<K, T> worker = new UdpWorker<>(data, context,
     * senderReceiver, index, executor); return worker; }
     */
    @Override
    public void close() {
        super.close();
        executor.stop();
    }

    @Override
    public String getPoolId() {
        return null;
    }

}
