package com.nomad.communication.tcp.server;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nomad.communication.MessageExecutor;
import com.nomad.communication.binders.AbstractServer;
import com.nomad.communication.binders.AbstractWorker;
import com.nomad.exception.SystemException;
import com.nomad.message.CommonAnswer;
import com.nomad.message.CommonMessage;
import com.nomad.message.MessageSenderReceiver;
import com.nomad.message.MessageSenderReceiverImpl;
import com.nomad.server.ServerContext;
import com.nomad.server.statistic.impl.StatisticCollectorImpl;
import com.nomad.statistic.StatisticCollector;
import com.nomad.utility.pool.PooledObjectImpl;

public  class TcpWorker<T extends CommonMessage, K extends CommonAnswer>  extends PooledObjectImpl  implements AbstractWorker<T, K> {
    private static Logger LOGGER = LoggerFactory.getLogger(TcpWorker.class);

    private Socket clientSocket = null;
    protected boolean stop = false;
    private final AbstractServer<T, K> server;
    private final int workerId;
    private InputStream input = null;
    private OutputStream output = null;
    private final MessageSenderReceiver msr;
    private final StatisticCollector statisticCollector = new StatisticCollectorImpl();
    private final MessageExecutor<T,K> executor;
    private final String serverName;


    public TcpWorker(final Socket clientSocket, final ServerContext context, final AbstractServer<T, K> server, final int index, final MessageExecutor<T, K> executor)
            throws Exception {
        this.clientSocket = clientSocket;
        this.server = server;
        this.workerId = index;
        this.msr = new MessageSenderReceiverImpl((byte) 0x1, context.getDataDefinitionService(null));
        this.executor=executor;
        serverName=context.getServerName();
    }

    @Override
    public int getWorkerId() {
        return workerId;
    }

    @Override
    @SuppressWarnings({ "unchecked" })
    public void run() {
        LOGGER.info("Started common  thread");
        long waitTime = System.currentTimeMillis();
        try {
            while (!stop) {
                input = clientSocket.getInputStream();
                output = clientSocket.getOutputStream();
                msr.reset();
                final T message = (T) msr.getObject(input);
                waitTime = waitTime - System.currentTimeMillis();
                final long start = System.currentTimeMillis();

                // stop system ???
                if (message == null) {
                    stop = true;
                    return;
                }
                final K answer = executor.execute(message);
                msr.storeObject(answer, output);
                statisticCollector.registerRequest(System.currentTimeMillis() - start, waitTime, msr.getInBytes(), msr.getOutBytes());
                waitTime = System.currentTimeMillis();
            }

        } catch (SocketException | EOFException e) {
            LOGGER.info(serverName+" Command session closed!");
        } catch (final IOException e) {
            LOGGER.error(serverName+" Error input or output", e);
        } catch (final SystemException e) {
            LOGGER.error(serverName+" Error input or output");
        } catch (final Throwable e) {
            LOGGER.error(serverName+" Error in " + this.getClass().getName(), e);
        } finally {

            try {
                if (output != null) {
                    output.close();
                }
            } catch (final Throwable e) {
            }
            try {
                if (input != null) {
                    input.close();
                }
            } catch (final Throwable e) {
                ;
            }
        }
        server.removeWorker(workerId);
        LOGGER.info(serverName+"Stop common thread");
        freeObject();
    }

    @Override
    public void stop() throws InterruptedException, IOException {
        stop=true;
        if (clientSocket != null) {
            clientSocket.close();
        }
    }

    @Override
    public StatisticCollector getStatistic() {
        return statisticCollector;
    }

    @Override
    public void closeObject() {
        try {
            clientSocket.close();
        } catch (final IOException e) {
            LOGGER.error(e.getMessage(),e);
        }
        executor.stop();

    }

    @Override
    protected long getSize() {
        return 0;
    }
}