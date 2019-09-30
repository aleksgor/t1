package com.nomad.communication.tcp.server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nomad.communication.MessageExecutor;
import com.nomad.communication.MessageExecutorFactory;
import com.nomad.communication.NetworkServer;
import com.nomad.communication.binders.AbstractServer;
import com.nomad.exception.SystemException;
import com.nomad.message.CommonAnswer;
import com.nomad.message.CommonMessage;
import com.nomad.model.CommonServerModel;
import com.nomad.server.ServerContext;
import com.nomad.server.ServerContext.ServiceName;
import com.nomad.server.Status;
import com.nomad.server.StoreModelService;

public class TcpServer<T extends CommonMessage, K extends CommonAnswer> extends AbstractServer<T, K> implements NetworkServer {

    protected static Logger LOGGER = LoggerFactory.getLogger(TcpServer.class);

    protected boolean isStopped = false;
    protected final ThreadPoolExecutor threadPool;
    protected volatile Status status = Status.SHUTDOWN;
    protected final MessageExecutorFactory<T, K> workerFactory;
    protected ServerSocket serverSocket;

    public TcpServer(final CommonServerModel serverModel, final ServerContext context, final String serverType, final MessageExecutorFactory<T, K> workerFactory)
            throws SystemException {
        super(serverModel, context, serverType);
        this.workerFactory = workerFactory;
        this.threadPool = new ThreadPoolExecutor(serverModel.getMinThreads(), serverModel.getMaxThreads(), serverModel.getKeepAliveTime(), TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>(), new ThreadPoolExecutor.CallerRunsPolicy()) {
            @Override
            protected void beforeExecute(final Thread t, final Runnable r) {
                t.setName("Session listener:" + serverModel.getHost() + ":" + serverModel.getPort());
            }
        };

    }

    @Override
    public void run() {

        LOGGER.info("Starting server:{} ", getClass().getName());
        openServerSocket();
        runInitScript();
        status = Status.STARTED;
        try {
            while (!isStopped()) {
                Socket clientSocket = null;
                try {
                    clientSocket = serverSocket.accept();
                } catch (final IOException e) {
                    if (isStopped()) {
                        LOGGER.info("Tcp server has stop flag " + serverModel);
                        isStopped = true;
                    }
                }
                if (clientSocket != null) {
                    LOGGER.info("New  server thread");
                    final int workerId = index.incrementAndGet();
                    final MessageExecutor<T, K> executor = workerFactory.getMessageExecutor(context, workerId, this);
                    final TcpWorker<T, K> worker = new TcpWorker<>(clientSocket, context, this, workerId, executor);
                    addWorker(worker);

                    threadPool.execute(worker);
                }
            }
        } catch (Exception x) {

        } finally {

            LOGGER.info("Session start stop");

            threadPool.shutdownNow();
            if (serverSocket != null) {
                try {
                    serverSocket.close();
                } catch (final IOException e) {
                    ;
                }
            }
            LOGGER.info("Session start stop2");
            LOGGER.info("Session server stopped");
        }
    }

    @Override
    protected void runInitScript() {

    }

    private synchronized boolean isStopped() {
        return this.isStopped;
    }

    @Override
    public Status getStatus() {
        return status;
    }

    @Override
    public void setStatus(final Status status) {
        serverInfo.setStatus(status);
        this.status = status;
    }

    @Override
    public synchronized void stop() {
        LOGGER.info("Server stopping: " + serverModel.getHost() + ":" + serverModel.getPort());
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
            if (threadPool != null) {
                threadPool.shutdownNow();
            }

        } catch (final IOException e) {
            LOGGER.error("Error closing server", e);
        } catch (final Throwable e) {
            LOGGER.error("Error closing server", e);
        }
        super.stop();
        status = Status.SHUTDOWN;
        isStopped = true;
        LOGGER.info("Server stopped: " + serverModel.getHost() + ":" + serverModel.getPort());
    }

    protected void openServerSocket() {
        LOGGER.info("Try to open host:{} port:{}", serverModel.getHost(), serverModel.getPort());
        try {
            if (serverModel.getHost() != null) {
                final InetAddress address = InetAddress.getByName(serverModel.getHost());
                serverSocket = new ServerSocket(serverModel.getPort(), 100, address);
            } else {
                serverSocket = new ServerSocket(serverModel.getPort());
            }
            LOGGER.info(" host:{} port:{}", serverModel.getHost(), serverModel.getPort() + " openned");
        } catch (final IOException e) {
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

    @Override
    public void close() {
        stop();
    }

}