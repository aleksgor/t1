package com.nomad.communication.udp.server;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nomad.communication.MessageExecutorFactory;
import com.nomad.communication.NetworkServer;
import com.nomad.communication.binders.AbstractServer;
import com.nomad.communication.udp.AbstractUDPSenderReceiver;
import com.nomad.communication.udp.UdpMessageImpl;
import com.nomad.exception.SystemException;
import com.nomad.message.CommonAnswer;
import com.nomad.message.CommonMessage;
import com.nomad.model.CommonServerModel;
import com.nomad.server.ServerContext;
import com.nomad.server.Status;

public class UdpServer <T extends CommonMessage, K extends CommonAnswer>   extends AbstractServer<T,K>  implements NetworkServer   {
    protected static Logger LOGGER = LoggerFactory.getLogger(UdpServer.class);

    private volatile AbstractUDPSenderReceiver udpSR;
    private volatile Status status = Status.SHUTDOWN;
    private final CommonServerModel serverModel;
    private final WorkerPool<T, K> workerPool;
    private DatagramSocket socket;

    public UdpServer(final CommonServerModel serverModel, final ServerContext context,  final String serverType,  final MessageExecutorFactory<T,K> workerFactory) throws SystemException{
        super(serverModel, context, serverType);
        this.serverModel=serverModel;
        workerPool = new WorkerPool<T, K>(serverModel.getMinThreads(), serverModel.getKeepAliveTime(), (int) serverModel.getKeepAliveTime(), context, false, workerFactory, this);

    }
    private void openSocket() throws Exception{
        socket = new DatagramSocket(serverModel.getPort(), InetAddress.getByName(serverModel.getHost()));
        udpSR=new AbstractUDPSenderReceiver(socket);
        status = Status.STARTED;

    }

    @Override
    public void run() {

        try {
            openSocket();
            workerPool.setSocket(socket);
            while (!Status.SHUTDOWN.equals(status)) {
                final UdpMessageImpl data= udpSR.receive();
                if(data!=null){
                    final UdpWorker<T, K> worker = workerPool.getObject();
                    worker.setDataInput(data);
                    new Thread( worker).start();
                }
            }

        } catch (final SocketException e) {
            LOGGER.info("!!Socket for :"+serverModel+" closed");
        } catch (final Throwable e) {
            LOGGER.error(e.getMessage()+" :"+serverModel.getHost()+":"+serverModel.getPort(),e);
        }finally{
            close();
        }


    }

    @Override
    public void close() {
        status = Status.SHUTDOWN;
        workerPool.close();
        if(socket!=null){
            socket.close();
        }
    }
    @Override
    public synchronized void stop() {
        LOGGER.info("Udp server stopping: " + serverModel.getHost() + ":" + serverModel.getPort());
        close();
        super.stop();
        status = Status.SHUTDOWN;
        LOGGER.info("Server stopped: " + serverModel.getHost() + ":" + serverModel.getPort());
    }

    public int checkClean() {
        return udpSR.checkClean();
    }

}
