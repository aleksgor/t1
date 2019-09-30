package com.nomad.communication.udp;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nomad.exception.SystemException;
import com.nomad.message.MessageHeader;
import com.nomad.message.MessageSenderReceiver;
import com.nomad.message.MessageSenderReceiverImpl;
import com.nomad.message.RawMessage;
import com.nomad.server.ServerContext;
import com.nomad.server.processing.ProxyProcessingInterface;
import com.nomad.server.statistic.impl.StatisticCollectorImpl;
import com.nomad.statistic.StatisticCollector;
import com.nomad.utility.pool.PooledObjectImpl;

public class UDPPooledProxyThread extends PooledObjectImpl implements Runnable {
    private final  static Logger LOGGER = LoggerFactory.getLogger(UDPPooledProxyThread.class);

    private UdpMessageImpl dataInput;
    private final AbstractUDPSenderReceiver sender;
    private final StatisticCollector statisticCollector = new StatisticCollectorImpl();
    private final MessageSenderReceiver msr;
    private final  ProxyProcessingInterface proxyProcessing;
    private final String serverName;


    public UDPPooledProxyThread(final ServerContext context, final AbstractUDPSenderReceiver sender, final int workerId, String serverName) throws SystemException  {

        this.sender = sender;
        msr = new MessageSenderReceiverImpl(context.getDataDefinitionService(null));
        this.serverName = serverName;
        proxyProcessing = context.getProxyProcessing(serverName);
    }


    public void setDataInput(UdpMessageImpl dataInput) {
        this.dataInput = dataInput;
    }

    @Override
    public void run() {
        try {

            long start = System.currentTimeMillis();
            long stopTime = System.currentTimeMillis();
            msr.reset();
            final InputStream input= new ByteArrayInputStream(dataInput.getData());
            final MessageHeader header = msr.getMessageHeader(input);
            stopTime = System.currentTimeMillis() - stopTime;
            start = System.currentTimeMillis();
            if(header==null || header.getCommand()==null ){
                return;
            }
            msr.getResult(input);

            LOGGER.debug("Server get message {}", header);


            final RawMessage message = proxyProcessing.execMessage(header, input, msr);

            final ByteArrayOutputStream out= new ByteArrayOutputStream();
            msr.assembleRawMessage(message, out);
            // TODO double arrays
            dataInput.setData(out.toByteArray());
            sender.send(dataInput);

            LOGGER.debug(" server send answer message {}", message);
            statisticCollector.registerRequest(System.currentTimeMillis() - start, stopTime, msr.getInBytes(), msr.getOutBytes());
            stopTime = System.currentTimeMillis();

        } catch (final SocketException e) {
            LOGGER.warn("SocketException: "+serverName+" was broken");
        } catch ( final EOFException e) {
            LOGGER.warn("EOFException:"+serverName);
        } catch ( final IOException e) {
            LOGGER.warn("IOException:"+serverName,e);
        } catch (final Throwable e) {
            LOGGER.error("Throwable:", e);
        } finally {

            freeObject();

        }
    }



    @Override
    public void closeObject() {

    }

    @Override
    protected long getSize() {
        return 0;
    }



}