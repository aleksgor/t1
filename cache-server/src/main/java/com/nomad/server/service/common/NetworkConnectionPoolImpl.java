package com.nomad.server.service.common;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nomad.exception.LogicalException;
import com.nomad.exception.SystemException;
import com.nomad.message.CommonAnswer;
import com.nomad.message.CommonMessage;
import com.nomad.model.CommonClientModel;
import com.nomad.model.ConnectStatus;
import com.nomad.server.ServerContext;
import com.nomad.utility.AbstractConnection;
import com.nomad.utility.NetworkConnectionPool;
import com.nomad.utility.pool.ObjectPoolImpl;

public abstract class NetworkConnectionPoolImpl<T extends CommonAnswer, K extends CommonMessage> extends ObjectPoolImpl<AbstractConnection<T, K>> implements
NetworkConnectionPool<T, K> {
    private static Logger LOGGER = LoggerFactory.getLogger(NetworkConnectionPoolImpl.class);

    protected volatile ConnectStatus status = ConnectStatus.OK;
    private ScheduledFuture<?> future = null;
    protected final CommonClientModel client;

    protected NetworkConnectionPoolImpl(final CommonClientModel client, final ServerContext context, final boolean dynamic, final String statisticGroupName) {
        super(client.getThreads(), client.getTimeout(), client.getTimeout() * 2, context, true, statisticGroupName);
        this.client=client;
    }

    @Override
    public ConnectStatus getConnectStatus() {
        return status;
    }

    @Override
    public void setConnectStatus(final ConnectStatus status) {
        this.status = status;
    }

    @Override
    public ConnectStatus getStatus() {
        return status;
    }

    @Override
    public void setStatus(final ConnectStatus status) {
        LOGGER.info("setStatus:"+status);
        if ((ConnectStatus.INACCESSIBLE.equals(status) || ConnectStatus.ERROR.equals(status)) && future == null) {
            future = context.getScheduledExecutorService().scheduleAtFixedRate(new CheckStatus(), 1, TimeUnit.SECONDS);
        } else if (ConnectStatus.OK.equals(status) && future != null) {
            context.getScheduledExecutorService().stop(future);
            future=null;
        }

        this.status = status;
    }


    @Override
    public void stop() {
        super.stop();
        if(future!=null){
            context.getScheduledExecutorService().stop(future);
        }
    }

    @Override
    public void close() {
        super.close();
        if(future!=null){
            context.getScheduledExecutorService().stop(future);
        }
    }

    @Override
    public abstract AbstractConnectionImpl<T, K> getNewPooledObject() throws SystemException, LogicalException;


    public abstract boolean connectTest();

    private  class CheckStatus implements Runnable {

        @Override
        public void run() {
            LOGGER.info("schedule!:");

            if(connectTest()){
                setStatus(ConnectStatus.OK);
            }else{
                setStatus(ConnectStatus.INACCESSIBLE);

            }
        }
    }

    @Override
    protected String getInternalStatisticName() {
        String intName = "local";
        if (client.getHost() != null) {
            intName = client.getHost() + "_" + client.getPort();
        }
        return intName;
    }

}