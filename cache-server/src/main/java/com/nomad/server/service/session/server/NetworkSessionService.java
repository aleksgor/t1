package com.nomad.server.service.session.server;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nomad.cache.commonclientserver.session.SessionMessageImpl;
import com.nomad.exception.SystemException;
import com.nomad.model.SessionCallBackServerModel;
import com.nomad.model.SessionData;
import com.nomad.model.session.SessionAnswer;
import com.nomad.model.session.SessionClientModel;
import com.nomad.model.session.SessionCommand;
import com.nomad.server.ServerContext;
import com.nomad.server.ServerStatus;
import com.nomad.server.SessionResult;
import com.nomad.server.SessionService;
import com.nomad.server.SessionState;
import com.nomad.server.service.session.SessionConnectionPool;
import com.nomad.session.SessionStateImpl;
import com.nomad.util.executorpool.ExecutorsPool;
import com.nomad.util.executorpool.PooledExecutor;

public class NetworkSessionService implements SessionService {

    private static Logger LOGGER = LoggerFactory.getLogger(NetworkSessionService.class);
    private final List<SessionConnectionPool> connectPools = new ArrayList<>();
    private List<SessionClientModel> models = new ArrayList<>();
    private ExecutorsPool execPool;
    private final ServerContext context;

    public NetworkSessionService(final List<SessionClientModel> models, final ServerContext context) {
        this.models = models;
        this.context = context;
    }

    @Override
    public boolean removeSession(final String sessions) {
        LOGGER.debug("Kill session:{}", sessions);
        final SessionMessageImpl message = new SessionMessageImpl();
        message.setSessionCommand(SessionCommand.KILL_SESSION);
        message.setSessionId(sessions);
        try {
            execAllTasks(message, false);
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return true;
    }

    @Override
    public SessionState getSessionState(final String sessionId, final String modelName, final String operation) {
        LOGGER.debug("getSessionState:{}", sessionId);
        final SessionMessageImpl message = new SessionMessageImpl();
        message.setSessionCommand(SessionCommand.CHECK_SESSION);
        message.setSessionId(sessionId);
        message.setModelName(modelName);
        message.setOperation(operation);

        SessionState result = null;
        try {
            final Collection<SessionAnswer> answers = execAllTasks(message, false);
            for (final SessionAnswer answer : answers) {
                if (result == null) {
                    result = getSessionState(answer);

                } else if (answer.getResultCode() == 0) { // optimistic variant
                    result = getSessionState(answer);
                }

            }
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
            return new SessionStateImpl(SessionResult.ERROR);
        }
        LOGGER.debug("server:" + context.getServerName() + ", getSessionState result:{}", result);
        new Exception().printStackTrace();
        return result;
    }

    private SessionState getSessionState(final SessionAnswer answer) {
        SessionResult sessionResult = SessionResult.ERROR;
        // OK(0),ERROR(-1),ACCESS_DENIED(-2),OPERATION_DENIED(-3),TIME_OUT(-4),NO_SESSION(-5);

        if (answer.getResultCode() == 0) {
            sessionResult = SessionResult.OK;
        } else if (answer.getResultCode() == -2) {
            sessionResult = SessionResult.ACCESS_DENIED;
        } else if (answer.getResultCode() == -3) {
            sessionResult = SessionResult.OPERATION_DENIED;
        } else if (answer.getResultCode() == -4) {
            sessionResult = SessionResult.TIME_OUT;
        } else if (answer.getResultCode() == -5) {
            sessionResult = SessionResult.NO_SESSION;
        }
        final SessionState result = new SessionStateImpl(sessionResult);
        result.setMainSession(answer.getParentSessionId());
        result.setSessionId(answer.getSessionId());
        result.getChildrenSessions().addAll(answer.getChildSessions());
        result.setUser(answer.getUserName());
        result.getRoles().addAll(answer.getRoles());

        return result;
    }

    @Override
    public SessionState startNewSession(String sessionId, String userName, String password) {

        LOGGER.debug("startNewSession:{}", sessionId);
        if (sessionId == null) {
            sessionId = UUID.randomUUID().toString();
        }
        final SessionMessageImpl message = new SessionMessageImpl();
        message.setSessionId(sessionId);
        message.setSessionCommand(SessionCommand.CREATE_SESSION);
        message.setUserName(userName);
        message.setPassword(password);

        SessionResult sessionResult = SessionResult.OK;
        try {
            Collection<SessionAnswer> answers = execAllTasks(message, false);
            for (SessionAnswer sessionAnswer : answers) {
                int code = sessionAnswer.getResultCode();
                if (code != 0) {
                    sessionResult = getSessionState(sessionAnswer).getResult();
                }
            }
        } catch (final Exception e) {
            LOGGER.error("Error start new session :" + e.getMessage()+"::"+sessionId,e);
            return null;
        }
        LOGGER.debug("startNewSession result:{}", sessionId);

        final SessionState result = new SessionStateImpl(sessionResult);

        result.setMainSession(sessionId);
        result.setSessionId(sessionId);
        result.getChildrenSessions().add(sessionId);
        return result;

    }

    @Override
    public void killOldSessions() {

    }

    @Override
    public boolean serverRegistering(final SessionCallBackServerModel sessionServerModel) {

        LOGGER.info("Register session client host:{}, port:{}", sessionServerModel.getHost(), sessionServerModel.getPort());
        final SessionMessageImpl message = new SessionMessageImpl();
        message.setSessionCommand(SessionCommand.REGISTER_CLIENT);
        message.setSessionId("");
        message.setModelName("");
        message.setOperation(sessionServerModel.getMaxThreads() + ":" + sessionServerModel.getHost() + ":" + sessionServerModel.getPort() + ":" + "1000");
        try {
            execAllTasks(message, true);
        } catch (final Exception e) {
            LOGGER.info("Register server unsuccessful host:{}, port:{}", sessionServerModel.getHost(), sessionServerModel.getPort());
        }

        return true;
    }

    @Override
    public boolean commit(final String sessionId) {
        LOGGER.debug("commit:{}", sessionId);
        if (sessionId == null) {
            return true;
        }

        final SessionMessageImpl message = new SessionMessageImpl();
        message.setSessionId(sessionId);
        message.setSessionCommand(SessionCommand.COMMIT);

        int resultCode = SessionResult.ERROR.getCode();
        try {

            final Collection<SessionAnswer> answers = execAllTasks(message, false);
            for (final SessionAnswer sessionAnswer : answers) {
                if (SessionResult.OK.getCode() != resultCode) {
                    resultCode = sessionAnswer.getResultCode();
                }
            }
        } catch (final Exception e) {
            LOGGER.error("Error start new session :" + e.getMessage()+"::"+sessionId,e);
            return false;
        }
        LOGGER.debug("startNewSession result:{}", sessionId);

        return SessionResult.OK.getCode() == resultCode;

    }

    @Override
    public void rollback(final String sessionId) {
        LOGGER.debug("rollback:{}", sessionId);
        if (sessionId == null) {
            return;
        }
        final SessionMessageImpl message = new SessionMessageImpl();
        message.setSessionId(sessionId);
        message.setSessionCommand(SessionCommand.ROLLBACK_SESSION);

        try {
            execAllTasks(message, false);
        } catch (final Exception e) {
            LOGGER.error("Error start new session :" + e.getMessage()+"::"+sessionId,e);
        }
        LOGGER.debug("rollback result:{}", sessionId);

    }

    private Collection<SessionAnswer> execAllTasks(final SessionMessageImpl message, final boolean allServers) throws InterruptedException, ExecutionException, Exception {
        final Collection<SessionAnswer> result = new ArrayList<>(connectPools.size());
        Collection<Future<SessionAnswer>> resultExec = null;
        PooledExecutor executor = null;
        try {
            final Collection<Callable<SessionAnswer>> requests = getTasks(message, allServers);
            if (requests.isEmpty()) {
                throw new Exception("No active servers");
            }
            executor = execPool.getObject();
            resultExec = executor.executeAll(requests);
        } finally {
            if (executor != null) {
                executor.freeObject();
            }
        }
        for (final Future<SessionAnswer> future : resultExec) {
            result.add(future.get());
        }
        return result;
    }

    private Collection<Callable<SessionAnswer>> getTasks(final SessionMessageImpl message, final boolean allServers) {
        final Collection<Callable<SessionAnswer>> tasks = new ArrayList<>(connectPools.size());

        for (final SessionConnectionPool connectPool : connectPools) {
            LOGGER.info("session ConnectPool:"+connectPool.getPoolId());
            if (ServerStatus.OK.equals(connectPool.getStatus()) || allServers) {
                LOGGER.info("session ConnectPool OK:"+connectPool.getPoolId());
                final SessionCallable callable = new SessionCallable(connectPool);
                callable.setMessage(message);
                tasks.add(callable);
            }
        }
        return tasks;
    }

    public void addClient(final SessionClientModel sessionModel) {
        execPool.addElements(sessionModel.getThreads());
    }

    @Override
    public void stop() {

        for (final SessionConnectionPool pool : connectPools) {
            pool.close();
        }
        connectPools.clear();
        execPool.close();
    }

    @Override
    public void start() throws SystemException {
        int maxThread = 0;
        for (final SessionClientModel sessionClientModel : models) {
            connectPools.add(new SessionConnectionPool(sessionClientModel, context));
            maxThread = Math.max(maxThread, sessionClientModel.getThreads());
        }
        execPool = new ExecutorsPool(maxThread, context, "network session  client exec Pool :"+context.getServerModel().getServerName(), 2000000);
    }

    @Override
    public SessionState startChildSession(final String parentSessionId, String childSessionId) {

        LOGGER.debug("startChildSession:{} parent:{}", childSessionId, parentSessionId);
        if (childSessionId == null) {
            childSessionId = UUID.randomUUID().toString();
        }
        final SessionMessageImpl message = new SessionMessageImpl();
        message.setSessionId(childSessionId);
        message.setMainSession(parentSessionId);
        message.setSessionCommand(SessionCommand.CREATE_CHILD_SESSION);
        SessionAnswer answer = null;
        try {
            final Collection<SessionAnswer> answers = execAllTasks(message, false);
            for (final SessionAnswer sessionAnswer : answers) {
                if (answer == null) {
                    answer = sessionAnswer;
                } else {
                    if (sessionAnswer.getResultCode() != 0) {
                        answer = sessionAnswer;
                    }
                }

            }
        } catch (final Exception e) {
            LOGGER.error("Error startChildSession  session :" + e.getMessage());
            return null;
        }
        final SessionState result = new SessionStateImpl(SessionResult.OK);
        result.setMainSession(answer.getParentSessionId());
        result.setSessionId(childSessionId);
        result.getChildrenSessions().addAll(answer.getChildSessions());
        LOGGER.debug(" start Child Session result:{}", result);
        return result;
    }

    @Override
    public Map<String, SessionData> getAllSessions() {
        // TODO Auto-generated method stub
        return null;
    }
    @Override
    public boolean isTrustService() {
        return false;
    }

}
