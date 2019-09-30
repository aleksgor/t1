package com.nomad.server.sessionserver;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nomad.client.ClientPooledInterface;
import com.nomad.exception.SystemException;
import com.nomad.model.session.SessionAnswer;
import com.nomad.model.session.SessionClientModel;
import com.nomad.model.session.SessionMessage;
import com.nomad.server.ServerContext;
import com.nomad.server.ServiceInterface;
import com.nomad.util.executorpool.ExecutorsPool;
import com.nomad.util.executorpool.PooledExecutor;

public class SessionServerCallBackClient implements ServiceInterface{
    private final ServerContext context;
    private static Logger LOGGER = LoggerFactory.getLogger(SessionServerCallBackClient.class);

    private volatile List<SessionClientConnectionPool> connectPools;
    private final ExecutorsPool executorsPool;
    private int maxThreads = 0;

    public SessionServerCallBackClient(final ServerContext context, final long timeout) {
        this.context = context;
        connectPools = new ArrayList<>();
        executorsPool = new ExecutorsPool(0, context, "Executor pool for session callBack client :"+context.getServerModel().getServerName(), timeout);
    }

    public void addCacheManager(final SessionClientModel sessionClient) throws SystemException  {

        try {
            final SessionClientConnectionPool pool = new SessionClientConnectionPool(sessionClient, context);
            connectPools.add(pool);
            maxThreads = maxThreads < sessionClient.getThreads() ? sessionClient.getThreads() : maxThreads;
            executorsPool.setNewSize(maxThreads);
        } catch (final Exception e) {
            throw new SystemException(e.getMessage(), e);
        }
    }

    public boolean sendAllMessage(final SessionMessage message) throws SystemException  {

        if (connectPools.size() > 0) {
            final PooledExecutor executor = executorsPool.getObject();
            final Collection<Callable<SessionAnswer>> tasks = new ArrayList<>(connectPools.size());
            for (final SessionClientConnectionPool pool : connectPools) {
                final ClientPooledInterface<SessionMessage, SessionAnswer> connection = pool.getObject();
                tasks.add(new Callable<SessionAnswer>() {
                    @Override
                    public SessionAnswer call() throws Exception {
                        final SessionAnswer result = connection.sendMessage(message);
                        connection.freeObject();
                        return result;
                    }

                });

            }
            List<Future<SessionAnswer>> results = null;
            try {
                results = executor.executeAll(tasks);
            } catch (InterruptedException e) {
               throw new SystemException(e.getMessage(),e);
            } finally {
                executor.freeObject();
            }
            if (results != null) {
                for (final Future<SessionAnswer> future : results) {
                    try {
                        final SessionAnswer resultMessage = future.get();
                        if (resultMessage.getResultCode() < 0) {
                            return false;
                        }
                    } catch (final Exception e) {
                        LOGGER.error(e.getMessage(), e);
                        return false;
                    }
                }
            }

        }
        return true;
    }

    @Override
    public void stop() {
        for (final SessionClientConnectionPool connect : connectPools) {
            connect.close();
        }
        connectPools.clear();
        executorsPool.close();

    }

    @Override
    public void start() throws SystemException {

    }
}
