package com.nomad.communication.binders;


import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nomad.communication.tcp.server.TcpServer;
import com.nomad.exception.SystemException;
import com.nomad.message.CommonAnswer;
import com.nomad.message.CommonMessage;
import com.nomad.model.CommonServerModel;
import com.nomad.server.CommonThreadListener;
import com.nomad.server.ServerContext;
import com.nomad.server.Status;
import com.nomad.server.statistic.ServerInformationImplMBean;
import com.nomad.server.statistic.impl.StatisticCollectorImpl;
import com.nomad.statistic.StatisticCollector;

public abstract class AbstractServer <T extends CommonMessage, K extends CommonAnswer> {

    protected static Logger LOGGER = LoggerFactory.getLogger(TcpServer.class);

    protected  Thread serverThread = null;
    protected final CommonServerModel serverModel;
    protected final ServerContext context;
    protected volatile Map<Integer, AbstractWorker<T,K>> threads = new ConcurrentHashMap<>();
    private volatile Status status = Status.SHUTDOWN;
    protected ServerInformationImplMBean serverInfo;
    private final  ScheduledFuture<?> statisticThread;
    protected final String serverType;
    protected AtomicInteger index=new AtomicInteger();


    public AbstractServer(final CommonServerModel serverModel, final ServerContext context,  final String serverType) throws SystemException {

        this.context = context;
        this.serverModel = serverModel;

        serverInfo = new ServerInformationImplMBean();
        serverInfo.setHost(serverModel.getHost());
        serverInfo.setPort(serverModel.getPort());
        serverInfo.setType(serverModel.getClass().getName());
        serverInfo.setThreads(serverModel.getMaxThreads());
        this.serverType=serverType;
        statisticThread = context.getScheduledExecutorService().scheduleAtFixedRate(new StatisticPublisher(serverType), 10, TimeUnit.SECONDS);

    }


    protected void runInitScript() {

    }


    public Status getStatus() {
        return status;
    }

    public void setStatus(final Status status) {
        serverInfo.setStatus(status);
        this.status = status;
    }

    public synchronized void stop() {
        LOGGER.info("Common server stopped: " + serverModel.getHost() + ":" + serverModel.getPort());
        status = Status.SHUTDOWN;
        statisticThread.cancel(true);
        for (final AbstractWorker<T,K> listener : threads.values()) {
            try {
                listener.stop();
            } catch (InterruptedException | IOException e) {
                LOGGER.error("Server stopped" + e.getMessage(), e);
            }
        }
        if(statisticThread!=null){
            statisticThread.cancel(true);
        }
    }



    public void addWorker(final AbstractWorker<T, K> worker) {
        threads.put(worker.getWorkerId(), worker);
        serverInfo.setBusy(threads.size());
    }
    public void removeWorker(final int workerId){
        threads.remove(workerId);
    }

    private class StatisticPublisher implements Runnable {
        private final ServerInformationImplMBean statisticInfo;

        public StatisticPublisher(final String serverType) {

            statisticInfo = new ServerInformationImplMBean();
            statisticInfo.setHost(serverModel.getHost());
            statisticInfo.setPort(serverModel.getPort());
            statisticInfo.setThreads(threads.size());

        }

        @Override
        public void run() {
            final StatisticCollector serverCollector = new StatisticCollectorImpl();
            for (final CommonThreadListener listener : threads.values()) {
                final StatisticCollector collector = listener.getStatistic();
                serverCollector.addStatisticCollector(collector.getCopy());
            }
            statisticInfo.setAvgThroughPut(serverCollector.getAvgTime());
            statisticInfo.setMinThroughPut(serverCollector.getMinTime());
            statisticInfo.setMaxThroughPut(serverCollector.getMaxTime());
            statisticInfo.setLoadFactor(((double) serverCollector.getRequestTime()) / (double) (serverCollector.getWaitTime() + serverCollector.getRequestTime()));

        }


    }
}