package com.nomad.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nomad.model.ListenerModel;
import com.nomad.server.ServerContext.ServiceName;
import com.nomad.server.service.storemodelservice.StoreModelServiceImpl;
import com.nomad.server.statistic.ServerInformationImplMBean;
import com.nomad.server.statistic.impl.StatisticCollectorImpl;
import com.nomad.statistic.StatisticCollector;

public class TCPListener implements ServerListener {

    private volatile int roundInterval = 10;

    protected ServerSocket serverSocket = null;
    private boolean isStopped = false;
    private ThreadPoolExecutor threadPool;
    protected ListenerModel listener;
    private Status status;
    private volatile ServerContext context;
    private static Logger LOGGER = LoggerFactory.getLogger(TCPListener.class);
    private volatile List<CommonThreadListener> childStopListeners = new Vector<>();
    private ScheduledFuture<?> statisticFuture;
    private String threadName;

    @Override
    public Status getStatus() {
        return status;
    }

    @Override
    public void setServerContext(final ServerContext context) {
        this.context = context;
    }

    public void setStatus(final Status status) {
        this.status = status;
    }

    @Override
    public ListenerModel getListener() {
        return listener;
    }

    @Override
    public void setListener(final ListenerModel listener) {
        this.listener = listener;
        this.listener.setProtocolVersion(listener.getProtocolVersion());
        if (listener.getStatus() == 1) {
            status = Status.SHUTDOWN;
        }
    }

    @Override
    public void run() {

        isStopped = false;
        openServerSocket();
        status = Status.READY;
        try {
            while (!isStopped) {
                Socket clientSocket = null;
                try {

                    clientSocket = serverSocket.accept();
                    clientSocket.setPerformancePreferences(0, 6, 1);

                } catch (final IOException e) {
                    if (isStopped) {
                        LOGGER.info("Listener :{} :{} stopped", listener.getHost(), listener.getPort());
                        return;
                    }
                    throw new RuntimeException("Error accepting client connection", e);
                }

                threadPool.execute(new ProxyThread(clientSocket, context, this));
            }
        } catch (Exception x) {
            LOGGER.error(x.getMessage(), x);
        }
    }

    @Override
    public synchronized void stop() {

        LOGGER.info(" Stop process :{} ", threadName);
        status = Status.SHUTDOWN;
        isStopped = true;
        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (final IOException e) {
                throw new RuntimeException("Error closing server", e);
            }
        }
        final List<CommonThreadListener> temporaryList = new ArrayList<>(childStopListeners);
        for (final CommonThreadListener listener : temporaryList) {
            try {
                listener.stop();
            } catch (IOException | InterruptedException e) {
                LOGGER.error(e.getMessage(), e);
            }

        }

        if (threadPool != null) {
            threadPool.shutdownNow();
            threadPool = null;
        }
        if (statisticFuture != null) {
            context.getScheduledExecutorService().stop(statisticFuture);
        }

    }

    protected void openServerSocket() {
        try {
            if (listener.getHost() != null && !"*".equals(listener.getHost())) {
                serverSocket = new ServerSocket(listener.getPort());
            } else {
                serverSocket = new ServerSocket(listener.getPort(), listener.getBacklog());
            }
        } catch (final IOException e) {
            throw new RuntimeException("Cannot open port: " + listener, e);
        }
    }

    @Override
    public void start() {

        threadName = "tcp listener " + listener.getHost() + ":" + listener.getPort();

        threadPool = new ThreadPoolExecutor(listener.getMinThreads(), listener.getMaxThreads(), 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(),
                new ThreadPoolExecutor.CallerRunsPolicy()) {
            @Override
            protected void beforeExecute(final Thread t, final Runnable r) {
                t.setName("listener:" + threadName);
            }
        };

        status = Status.READY;

        final StatisticPublisher statisticPublisher = new StatisticPublisher(this);
        statisticFuture = context.getScheduledExecutorService().scheduleAtFixedRate(statisticPublisher, roundInterval, TimeUnit.SECONDS);
    }

    private class StatisticPublisher implements Runnable {
        private final ServerInformationImplMBean statisticInfo;

        public StatisticPublisher(final ServerListener thisListener) {

            statisticInfo = new ServerInformationImplMBean();
            statisticInfo.setListener(thisListener);
            statisticInfo.setHost(listener.getHost());
            statisticInfo.setPort(listener.getPort());
            statisticInfo.setThreads(childStopListeners.size());
            context.getInformationPublisherService().publicData(statisticInfo,
                    ((StoreModelServiceImpl) context.get(ServiceName.STORE_MODEL_SERVICE)).getServerModel().getServerName(), "HttpListner",
                    listener.getHost() + "-" + listener.getPort());

        }

        @Override
        public void run() {
            final StatisticCollector serverCollector = new StatisticCollectorImpl();
            for (final CommonThreadListener listener : childStopListeners) {
                final StatisticCollector collector = listener.getStatistic();
                serverCollector.addStatisticCollector(collector.getCopy());
            }
            statisticInfo.setAvgThroughPut(serverCollector.getAvgTime());
            statisticInfo.setMinThroughPut(serverCollector.getMinTime());
            statisticInfo.setMaxThroughPut(serverCollector.getMaxTime());
            statisticInfo.setLoadFactor(((double) serverCollector.getRequestTime()) / (double) (serverCollector.getWaitTime() + serverCollector.getRequestTime()));

        }
    }

    @Override
    public void addStopListener(final CommonThreadListener listener) {
        childStopListeners.add(listener);

    }

    @Override
    public void removeStopListener(final CommonThreadListener listener) {
        childStopListeners.remove(listener);

    }

    @Override
    public String getThreadName() {
        return threadName;
    }

}
