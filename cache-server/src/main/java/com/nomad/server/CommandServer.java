package com.nomad.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nomad.InternalTransactDataStore;
import com.nomad.exception.SystemException;
import com.nomad.model.ServerModel;
import com.nomad.model.command.CommandServerModel;

public class CommandServer implements Runnable {

    private ServerSocket serverSocket = null;
    private boolean isStopped = false;
    private Thread runningThread = null;
    private final ExecutorService threadPool;
    private final ServerModel server;

    private volatile InternalTransactDataStore store;
    private static Logger LOGGER = LoggerFactory.getLogger(CommandServer.class);


    public CommandServer(final ServerModel server, final InternalTransactDataStore store) throws SystemException {

        final CommandServerModel commandServer=server.getCommandServerModel();
        this.threadPool = new ThreadPoolExecutor(commandServer.getMinThreads(), commandServer.getMaxThreads(), commandServer.getKeepAliveTime(),
                TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(), new ThreadPoolExecutor.CallerRunsPolicy()) {
            @Override
            protected void beforeExecute(final Thread t, final Runnable r) {
                t.setName("session listener:" + commandServer.getHost() + ":" + commandServer.getPort());
            }
        };

        this.store=store;
        this.server=server;
    }

    @Override
    public void run() {
        synchronized (this) {
            this.runningThread = Thread.currentThread();
        }
        openServerSocket();
        while (!isStopped()) {
            Socket clientSocket = null;
            try {
                clientSocket = this.serverSocket.accept();
            } catch (final IOException e) {
                if (isStopped()) {
                    return;
                }
                throw new RuntimeException("Error accepting client connection", e);
            }
            LOGGER.info("new command server thread");
            this.threadPool.execute(new CommandThread(clientSocket, store, server));
        }
        runningThread.interrupt();
        this.threadPool.shutdown();
        LOGGER.info("command server stoped");

    }

    private synchronized boolean isStopped() {
        return this.isStopped;
    }

    public synchronized void stop() {
        this.isStopped = true;
        try {
            this.serverSocket.close();
        } catch (final IOException e) {
            LOGGER.error("Error closing server", e);
        } catch (final Throwable e) {
            LOGGER.error("Error closing server", e);
        }
    }

    private void openServerSocket() {
        LOGGER.info("try to open port:{}",server.getCommandServerModel().getPort());
        try {
            this.serverSocket = new ServerSocket(this.server.getCommandServerModel().getPort());
        } catch (final IOException e) {
            throw new RuntimeException("Cannot open port: "+server.getCommandServerModel().getPort(), e);
        }
    }
}
