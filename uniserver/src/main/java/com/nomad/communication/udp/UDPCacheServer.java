package com.nomad.communication.udp;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
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
import com.nomad.server.CommonThreadListener;
import com.nomad.server.ServerContext;
import com.nomad.server.ServerContext.ServiceName;
import com.nomad.server.ServerListener;
import com.nomad.server.Status;
import com.nomad.server.StoreModelService;
import com.nomad.server.statistic.ServerInformationImplMBean;
import com.nomad.server.statistic.impl.StatisticCollectorImpl;
import com.nomad.statistic.StatisticCollector;

public class UDPCacheServer implements ServerListener {

    private volatile AbstractUDPSenderReceiver udpSR;
    private  DatagramSocket socket;

    private volatile int roundInterval = 10;
    private ThreadPoolExecutor threadPool;
    private ListenerModel listener;
    private Status status;
    private volatile ServerContext context;
    private static Logger LOGGER = LoggerFactory.getLogger(UDPCacheServer.class);
    private volatile List<CommonThreadListener> childStopListeners = new Vector<>();
    private ScheduledFuture<?> statisticFuture;
    private String threadName;
    private  ProxyThreadPool workerPool;


    public UDPCacheServer(){
    }

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
        workerPool= new ProxyThreadPool(listener.getMaxThreads(), listener.getBacklog(), listener.getBacklog(), context, false);
    }

    @Override
    public void run() {

        openServerSocket();
        workerPool.setSocket(socket);
        status = Status.READY;
        try {
            while (!Status.SHUTDOWN.equals(status)) {
                final UdpMessageImpl data= udpSR.receive();
                if(data!=null){
                    final UDPPooledProxyThread thread = workerPool.getObject();
                    thread.setDataInput(data);
                    new Thread( thread).start();
                }
            }

        } catch (final SocketException e) {
            LOGGER.info("Socket for :"+listener+" closed");
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(),e);
        }finally{
            stop();
        }


    }

    @Override
    public synchronized void stop() {

        LOGGER.info(" Stop process :{} ", threadName);
        status = Status.SHUTDOWN;
        socket.close();

        final List<CommonThreadListener> listeners = new ArrayList<>(childStopListeners);
        for (final CommonThreadListener listener : listeners) {
            try {
                listener.stop();
            } catch (IOException | InterruptedException e) {
                LOGGER.error(e.getMessage(), e);
            }

        }
        if(threadPool!=null){
            threadPool.shutdownNow();
            threadPool=null;
        }
        if (statisticFuture != null) {
            context.getScheduledExecutorService().stop(statisticFuture);
        }

    }

    private void openServerSocket() {
        try {
            socket = new DatagramSocket(listener.getPort(), InetAddress.getByName(listener.getHost()));
            udpSR=new AbstractUDPSenderReceiver(socket);
        } catch ( final Exception e) {
            LOGGER.error(e.getMessage(),e);
        }
    }

    @Override
    public void start() {

        threadName = "UDP server " + listener.getHost() + ":" + listener.getPort();

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
            context.getInformationPublisherService().publicData(statisticInfo, ((StoreModelService) context.get(ServiceName.STORE_MODEL_SERVICE)).getServerModel().getServerName(),
                    "HttpListner",
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
