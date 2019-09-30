package com.nomad.communication.udp.server;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import com.nomad.communication.MessageExecutor;
import com.nomad.communication.binders.AbstractWorker;
import com.nomad.communication.udp.AbstractUDPSenderReceiver;
import com.nomad.communication.udp.UdpMessageImpl;
import com.nomad.exception.SystemException;
import com.nomad.message.CommonAnswer;
import com.nomad.message.CommonMessage;
import com.nomad.message.MessageSenderReceiver;
import com.nomad.message.MessageSenderReceiverImpl;
import com.nomad.server.ServerContext;
import com.nomad.server.statistic.impl.StatisticCollectorImpl;
import com.nomad.statistic.StatisticCollector;
import com.nomad.utility.pool.PooledObjectImpl;

public class UdpWorker <T extends CommonMessage, K extends CommonAnswer> extends PooledObjectImpl implements AbstractWorker < T, K> {


    private UdpMessageImpl dataInput;
    private final AbstractUDPSenderReceiver sender;
    private final StatisticCollector statisticCollector = new StatisticCollectorImpl();
    private final MessageExecutor<T,K> executor;
    private final MessageSenderReceiver msr;
    @SuppressWarnings("unused")
    private final ServerContext context;
    private  long sendBytes = 0;


    public UdpWorker(final ServerContext context, final AbstractUDPSenderReceiver sender, final int workerId, final MessageExecutor<T, K> executor) throws SystemException {

        this.sender = sender;
        this.executor=executor;
        this.context=context;
        msr = new MessageSenderReceiverImpl((byte) 0x1, context.getDataDefinitionService(null));
    }

    public void setDataInput(UdpMessageImpl dataInput) {
        this.dataInput = dataInput;
    }

    @Override
    public void run() {

        try {
            msr.reset();
            @SuppressWarnings("unchecked")
            final T message = (T) msr.getObject(new ByteArrayInputStream(dataInput.getData()));
            final K answer = executor.execute(message);
            final ByteArrayOutputStream outputStream = new ByteArrayOutputStream(1024);
            msr.storeObject(answer,outputStream);
            dataInput.setData(outputStream.toByteArray());

            sendBytes = msr.getOutBytes() + msr.getInBytes();

            sender.send(dataInput);
            dataInput.clean();
        } catch (final Exception e) {
            e.printStackTrace();
        }finally{
            freeObject();
        }
    }
    @Override
    public void stop() throws InterruptedException, IOException {
        executor.stop();
    }
    @Override
    public StatisticCollector getStatistic() {
        return statisticCollector;
    }
    @Override
    public int getWorkerId() {
        return 0;
    }
    @Override
    public void closeObject() {

    }
    @Override
    protected long getSize() {
        return sendBytes;
    }



}