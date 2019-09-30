package com.nomad.server.service.session;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nomad.cache.commonclientserver.session.SessionMessageImpl;
import com.nomad.client.ClientPooledInterface;
import com.nomad.communication.binders.ServerFactory;
import com.nomad.exception.LogicalException;
import com.nomad.exception.SystemException;
import com.nomad.model.session.SessionAnswer;
import com.nomad.model.session.SessionClientModel;
import com.nomad.model.session.SessionCommand;
import com.nomad.model.session.SessionMessage;
import com.nomad.server.CacheServerConstants;
import com.nomad.server.ServerContext;
import com.nomad.server.ServerStatus;
import com.nomad.utility.pool.ObjectPoolImpl;

public class SessionConnectionPool extends ObjectPoolImpl<ClientPooledInterface<SessionMessage, SessionAnswer>> {

    private static Logger LOGGER = LoggerFactory.getLogger(SessionConnectionPool.class);
    private final SessionClientModel model;
    private final ServerContext context;
    private volatile ServerStatus status;
    private volatile ScheduledFuture<?> schedule;

    public SessionConnectionPool(final SessionClientModel model, final ServerContext context) throws SystemException {
        super(model.getThreads(), 1000, 2000, context, false, CacheServerConstants.Statistic.SESSION_CLIENT_GROUP_NAME);
        statisticGroupName = CacheServerConstants.Statistic.SESSION_CLIENT_GROUP_NAME;
        this.context = context;
        this.model = model;
        status = ServerStatus.OK;

    }

    @Override
    public String getPoolId() {

        return "Session client:" + model.getHost() + ":" + model.getPort();
    }

    @Override
    public ClientPooledInterface<SessionMessage, SessionAnswer> getNewPooledObject() throws SystemException, LogicalException {

        try {
            final ClientPooledInterface<SessionMessage, SessionAnswer> client = ServerFactory.getPooledClient(model, context);
            return client;
        } catch (final Exception e) {
            setStatus(ServerStatus.ERROR);
            LOGGER.error("Cannot connect to " + model.getHost() + ":" + model.getPort());
            throw e;
        }

    }

    public ServerStatus getStatus() {
        return status;
    }

    public void setStatus(final ServerStatus status) {
        LOGGER.info(" --Pool:{} set status:{}", getPoolId(), status);
        this.status = status;
        if (status.getStatusCode() > 1 && schedule == null) {

            schedule = context.getScheduledExecutorService().scheduleAtFixedRate(new Runnable() {
                @Override
                public void run() {
                    try {
                        LOGGER.info(" Try  to connect: {}", getPoolId());
                        final SessionMessage message = new SessionMessageImpl();
                        message.setSessionCommand(SessionCommand.GET_STATUS);
                        ClientPooledInterface<SessionMessage, SessionAnswer> client = getObject();
                        try {
                            final SessionAnswer answer = client.sendMessage(message);
                            if (answer.getResultCode() == 0) {
                                LOGGER.info("pool{} connection successful ", getPoolId());
                                setStatus(ServerStatus.OK);
                                context.getScheduledExecutorService().stop(schedule);
                            }
                        } finally {
                            client.freeObject();
                        }
                    } catch (final Exception e) {

                        LOGGER.error("Error connect to:" + model.getHost() + model.getPort());
                    }
                }

            }, 1000, TimeUnit.MILLISECONDS);
        }
    }

    @Override
    public void stop() {
        if (schedule != null) {
            context.getScheduledExecutorService().stop(schedule);
        }
        super.stop();
    }

    @Override
    public String toString() {
        return "SessionConnectionPool [model=" + model + ", context=" + context + ", status=" + status + ", sck=" + schedule + "]";
    }

}
