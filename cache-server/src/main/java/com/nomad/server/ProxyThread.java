package com.nomad.server;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nomad.core.ProxyProcessing;
import com.nomad.exception.ErrorCodes;
import com.nomad.exception.SystemException;
import com.nomad.message.MessageHeader;
import com.nomad.message.MessageSenderReceiver;
import com.nomad.message.MessageSenderReceiverImpl;
import com.nomad.message.RawMessage;
import com.nomad.server.service.storemodelservice.StoreModelServiceImpl;
import com.nomad.server.statistic.impl.StatisticCollectorImpl;
import com.nomad.statistic.StatisticCollector;

public class ProxyThread implements Runnable,CommonThreadListener {
    private Socket clientSocket = null;
    private final  static Logger LOGGER = LoggerFactory.getLogger(ProxyThread.class);
    private InputStream input = null;
    private OutputStream output = null;
    private final  int port;
    private final  String host;
    private final  ProxyProcessing proxyProcessing;
    private volatile ServerContext context;
    private volatile boolean stop=false;
    private final ServerListener severListener;

    private final StatisticCollector statisticCollector = new StatisticCollectorImpl();
    private String  serverName;

    public ProxyThread(final Socket clientSocket, final ServerContext context, final ServerListener severListener) throws SystemException  {

        this.clientSocket = clientSocket;
        port=clientSocket.getLocalPort();
        host=clientSocket.getInetAddress().getHostName();

        final StoreModelServiceImpl server = (StoreModelServiceImpl) context.get(ServerContext.ServiceName.STORE_MODEL_SERVICE);
        proxyProcessing= new ProxyProcessing(context, host+":"+port);
        this.context=context;
        this.severListener=severListener;
        serverName=server.getServerModel().getServerName();

    }

    @Override
    public void run() {
        try {

            input = new BufferedInputStream(clientSocket.getInputStream());
            output = new BufferedOutputStream(clientSocket.getOutputStream());
            serverName+=" "+clientSocket.getLocalSocketAddress();
            severListener.addStopListener(this);
            final MessageSenderReceiver msr = new MessageSenderReceiverImpl(context.getDataDefinitionService(null));
            long start = System.currentTimeMillis();
            long stopTime = System.currentTimeMillis();
            while (!stop) {
                msr.reset();
                final MessageHeader header = msr.getMessageHeader(input);
                stopTime = System.currentTimeMillis() - stopTime;
                start = System.currentTimeMillis();
                if(header==null || header.getCommand()==null ){
                    break;
                }
                msr.getResult(input);

                LOGGER.debug("Server get message {}", header);


                final RawMessage message = proxyProcessing.execMessage(header, input, msr);
                msr.assembleRawMessage(message, output);

                output.flush();

                LOGGER.debug(" server send answer message {}", message);
                statisticCollector.registerRequest(System.currentTimeMillis() - start, stopTime, msr.getInBytes(), msr.getOutBytes());
                stopTime = System.currentTimeMillis();
            }
        } catch (final SocketException e) {
            LOGGER.warn("SocketException: host:"+host+" port:"+port+" was broken");
        } catch ( final EOFException e) {
            LOGGER.warn("EOFException:"+serverName);
        } catch ( final IOException e) {
            LOGGER.warn("IOException:"+serverName,e);
        } catch ( final SystemException e) {
            if(!ErrorCodes.Connect.ERROR_CONNECT_EOF.equals(e.getMessage())){
                LOGGER.warn("IOException:"+serverName);
            }
            
        } catch (final Throwable e) {
            LOGGER.error("Throwable in " + serverName + " exception:" + e, e);
        } finally {
            try {
                stop();
            } catch (final Exception e) {
                LOGGER.error("Throwable:", e);
            }

        }
        severListener.removeStopListener(this);

    }


    private void breakConnect(){
        try {
            if(input!=null ){
                input.close();
            }
        } catch (final IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
        try {
            if(output!=null ){
                output.close();
            }
        } catch (final IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
        try {
            if(clientSocket!=null ){
                clientSocket.close();
            }
        } catch (final IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }



    @Override
    public void stop() throws InterruptedException, IOException {
        stop=true;
        breakConnect();
        proxyProcessing.close();

    }

    @Override
    public StatisticCollector getStatistic() {
        final StatisticCollector result = statisticCollector.getCopy();
        statisticCollector.reset();
        return result;
    }
}