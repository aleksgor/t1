package com.nomad.server.service.session.synchronizesession;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nomad.cache.commonclientserver.session.SessionMessageImpl;
import com.nomad.exception.SystemException;
import com.nomad.model.ConnectStatus;
import com.nomad.model.SessionData;
import com.nomad.model.session.SessionAnswer;
import com.nomad.model.session.SessionClientModel;
import com.nomad.model.session.SessionCommand;
import com.nomad.model.session.SessionMessage;
import com.nomad.model.session.SessionServerModel;
import com.nomad.server.ServerContext;
import com.nomad.server.SynchronizeSessionService;
import com.nomad.util.executorpool.ExecutorsPool;
import com.nomad.util.executorpool.PooledExecutor;

public class SynchronizeSessionServiceImpl implements SynchronizeSessionService {
    private static Logger LOGGER = LoggerFactory.getLogger(SynchronizeSessionService.class);

    private final List<SynchronizationSessionConnectPool> pools = new Vector<>();
    private ExecutorsPool execPool;
    private final Collection<SessionClientModel> mirrors;
    private final SessionServerModel server;
    private final ServerContext context;
    private int maxThread = 1;

    public SynchronizeSessionServiceImpl(final SessionServerModel model, final ServerContext context) {
        server = model;
        mirrors = model.getMirrors();
        this.context = context;
    }

    @Override
    public void start() throws SystemException {
        LOGGER.info("Synchronization session service  model:{} ", server);

        for (final SessionClientModel clientModel : mirrors) {
            final SynchronizationSessionConnectPool pool = new SynchronizationSessionConnectPool(clientModel, context);
            pools.add(pool);
            // test connection
            maxThread = Math.max(maxThread, clientModel.getThreads());
        }
        execPool = new ExecutorsPool(maxThread, context, "Session client exec Pool", 1000);
        LOGGER.info("Init save service server model:{} successful ", server);

    }

    @Override
    public void stop() {
        for (final SynchronizationSessionConnectPool pool : pools) {
            pool.stop();
        }
        if(execPool!=null){
            execPool.close();
        }

    }

    @Override
    public void removeSession(final String sessionId) {
        final SessionMessage message = new SessionMessageImpl();
        message.setSessionCommand(SessionCommand.SYNC_REMOVE_SESSION);
        message.setSessionId(sessionId);
        try {
            execAllTasks(message, false);
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    @Override
    public void startNewSession(final String sessionId) {
        final SessionMessage message = new SessionMessageImpl();
        message.setSessionCommand(SessionCommand.SYNC_START_NEW_SESSION);
        message.setSessionId(sessionId);
        try {
            execAllTasks(message, false);
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    @Override
    public void startChildSession(final String parentSessionId, final String childSessionId) {
        final SessionMessage message = new SessionMessageImpl();
        message.setSessionCommand(SessionCommand.SYNC_START_NEW_CHILD_SESSION);
        message.setMainSession(parentSessionId);
        message.setSessionId(childSessionId);
        try {
            execAllTasks(message, false);
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    private Collection<SessionAnswer> execAllTasks(final SessionMessage message, final boolean allServers) throws InterruptedException, ExecutionException, Exception {
        final Collection<SessionAnswer> result = new ArrayList<>(pools.size());
        PooledExecutor executor = null;
        try {
            final Collection<Callable<SessionAnswer>> requests = getTasks(message, allServers);
            if (requests.isEmpty()) {
                LOGGER.debug("SynchronizeSessionServiceImpl: no active connect");
                return Collections.emptyList();
            }
            executor = execPool.getObject();
            final List<Future<SessionAnswer>> results = executor.executeAll(requests);
            for (final Future<SessionAnswer> future : results) {
                result.add(future.get());
            }
        } finally {
            if (executor != null) {
                executor.freeObject();
            }
        }
        return result;
    }

    private Collection<Callable<SessionAnswer>> getTasks(final SessionMessage message, final boolean allServers) {
        final Collection<Callable<SessionAnswer>> tasks = new ArrayList<>(pools.size());
        for (final SynchronizationSessionConnectPool connectPool : pools) {
            if (ConnectStatus.OK.equals(connectPool.getStatus()) || allServers) {
                final SynchronizationSessionCallable callable = new SynchronizationSessionCallable(connectPool, message);
                tasks.add(callable);
            }
        }
        return tasks;
    }

    @Override
    public SessionAnswer getSessionState(final String sessionId, final String modelName, final String operation) {
        final SessionMessage message = new SessionMessageImpl();
        message.setSessionCommand(SessionCommand.SYNC_GET_SESSION_STATE);
        message.setSessionId(sessionId);
        message.setModelName(modelName);
        message.setOperation(operation);
        SessionAnswer result = null;
        try {
            final Collection<SessionAnswer> answers = execAllTasks(message, false);
            for (final SessionAnswer sessionAnswer : answers) {
                if (result == null) {
                    result = sessionAnswer;
                } else {
                    if (sessionAnswer.getResultCode() == 0) {
                        result = sessionAnswer;
                    }
                }
            }
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }

        return result;
    }

    @Override
    public Map<String, SessionData> getAllSessionData() {
        final SessionMessage message = new SessionMessageImpl();
        message.setSessionCommand(SessionCommand.SYNC_GET_ALL_SESSIONS);
        Map<String, SessionData> result = null;
        try {
            final Collection<SessionAnswer> answers = execAllTasks(message, false);
            for (final SessionAnswer sessionAnswer : answers) {
                if (sessionAnswer.getSyncData() != null) {
                    if (result == null) {
                        result = sessionAnswer.getSyncData().getChildSessions();
                    } else {
                        if (sessionAnswer.getResultCode() == 0) {
                            result.putAll(sessionAnswer.getSyncData().getChildSessions());
                        }
                    }
                }
            }
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return result;
    }

}
