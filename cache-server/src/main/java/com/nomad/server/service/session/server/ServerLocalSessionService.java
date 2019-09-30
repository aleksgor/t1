package com.nomad.server.service.session.server;

import java.util.Map;

import com.nomad.cache.commonclientserver.session.SessionMessageImpl;
import com.nomad.exception.SystemException;
import com.nomad.model.SessionData;
import com.nomad.model.session.SessionAnswer;
import com.nomad.model.session.SessionCommand;
import com.nomad.model.session.SessionMessage;
import com.nomad.model.session.SessionServerModel;
import com.nomad.server.ServerContext;
import com.nomad.server.SessionResult;
import com.nomad.server.SessionState;
import com.nomad.server.SynchronizeSessionService;
import com.nomad.server.service.session.synchronizesession.SynchronizeSessionServiceImpl;
import com.nomad.server.sessionserver.SessionServerCallBackClient;
import com.nomad.session.SessionStateImpl;

public class ServerLocalSessionService extends LocalSessionService {

    private final SessionServerCallBackClient callbackClient;
    private final SynchronizeSessionService synchronizeSessionService;

    public ServerLocalSessionService(final SessionServerModel sessionServerModel, final SessionServerCallBackClient callbackClient, final ServerContext context) {
        super(sessionServerModel, context);
        this.callbackClient = callbackClient;
        synchronizeSessionService = new SynchronizeSessionServiceImpl(sessionServerModel, context);
    }

    @Override
    public boolean commit(final String sessionId) {
        LOGGER.debug(" server commit {}!", sessionId);
        final SessionMessageImpl message = new SessionMessageImpl();
        final SessionData mainSessionData = sessions.get(sessionId);
        final SessionData sessionData = mainSessionData.searchSession(sessionId);

        message.setSessionCommand(SessionCommand.COMMIT_PHASE1);
        message.setSessionId(sessionId);
        message.setMainSession(mainSessionData.getSessionId());
        message.getSessionIds().addAll(sessionData.getAllSessions());
        try {
            if (callbackClient.sendAllMessage(message)) {
                message.setSessionCommand(SessionCommand.COMMIT_PHASE2);
                if (callbackClient.sendAllMessage(message)) {
                    return true;
                }
                message.setSessionCommand(SessionCommand.ROLLBACK_SESSION);
                callbackClient.sendAllMessage(message);
                return false;
            } else {
                message.setSessionCommand(SessionCommand.ROLLBACK_SESSION);
                callbackClient.sendAllMessage(message);
            }
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return false;
    }

    @Override
    public void rollback(final String sessionId) throws SystemException {
        final SessionData sessionData = sessions.get(sessionId);
        rollback0(sessionData, sessionId);
    }

    public void rollback0(final SessionData mainSessionData, final String sessionId) throws SystemException {
        final SessionData sessionData = mainSessionData.searchSession(sessionId);
        final SessionMessageImpl message = new SessionMessageImpl();
        message.setSessionCommand(SessionCommand.ROLLBACK_SESSION);
        message.setSessionId(sessionId);
        message.setMainSession(mainSessionData.getSessionId());
        message.getSessionIds().addAll(sessionData.getAllSessions());

       callbackClient.sendAllMessage(message);
    }

    @Override
    public boolean removeSession(final String sessionId) {
        final boolean result = removeSessionSynchronization(sessionId);
        synchronizeSessionService.removeSession(sessionId);
        return result;
    }

    public boolean removeSessionSynchronization(final String sessionId) {
        final SessionState state = super.getSessionState(sessionId, null, null);
        final boolean result = super.removeSession(sessionId);
        if (SessionResult.OK.equals(state.getResult())) {
            final SessionMessage message = new SessionMessageImpl();
            message.setSessionCommand(SessionCommand.ROLLBACK_SESSION);
            message.setSessionId(sessionId);
            message.setMainSession(state.getMainSession());
            message.getSessionIds().addAll(state.getChildrenSessions());
            try {
                callbackClient.sendAllMessage(message);
            } catch (final Exception e) {
                LOGGER.error(e.getMessage(), e);
            }

        }
        return result;
    }

    @Override
    public SessionState startNewSession(final String sessionId, String user, String password) {
        final SessionState result = super.startNewSession(sessionId, user, password);
        synchronizeSessionService.startNewSession(result.getSessionId());
        return result;
    }

    public SessionState startNewSessionSynchronization(final String sessionId, String user, String password) {
        final SessionState result = super.startNewSession(sessionId, user, password);
        return result;
    }

    @Override
    public SessionState startChildSession(final String parentSessionId, final String childSessionId) {
        final SessionState result = super.startChildSession(parentSessionId, childSessionId);
        synchronizeSessionService.startChildSession(result.getMainSession(), result.getSessionId());
        return result;
    }

    @Override
    public SessionState getSessionState(final String sessionId, final String modelName, final String operation) {
        SessionState result = super.getSessionState(sessionId, modelName, operation);
        if (result.getResult().equals(SessionResult.NO_SESSION)) {
            final SessionAnswer sessionAnswer = synchronizeSessionService.getSessionState(sessionId, modelName, operation);

            if (sessionAnswer != null && sessionAnswer.getResultCode() == 0 && sessionAnswer.getSyncData() != null) {
                final SessionData sessionData = sessionAnswer.getSyncData();
                sessionData.updateDate();
                for (final String session : sessionData.getAllSessions()) {
                    sessions.put(session, sessionData);
                }
                result= super.getSessionState(sessionId, modelName, operation);
            }
        }
        return result;
    }

    public SessionStateData getSessionStateSync(final String sessionId, final String modelName, final String operation) {
        final SessionData sessionData = sessions.get(sessionId);
        SessionStateData result;
        if (sessionData == null) {
            result = new SessionStateData(new SessionStateImpl(SessionResult.NO_SESSION),null);
            return result;
        }
        final SessionState sessionState = super.getSessionState(sessionId, modelName, operation);

        return new SessionStateData(sessionState, sessionData);
    }

    public SessionState startChildSessionSync(final String parentSessionId, final String childSessionId) {
        return super.startChildSession(parentSessionId, childSessionId);
    }

    @Override
    public void start() throws SystemException {
        LOGGER.info("ServerLocalSessionService run start");
        super.start();
        callbackClient.start();
        synchronizeSessionService.start();

        Map<String, SessionData> externalData = synchronizeSessionService.getAllSessionData();
        if (externalData != null) {
            sessions.putAll(externalData);
        }
        LOGGER.info("ServerLocalSessionService started");

    }

    @Override
    public void stop() {
        LOGGER.info("ServerLocalSessionService run stop");
        super.stop();
        callbackClient.stop();

        synchronizeSessionService.stop();
        LOGGER.info("ServerLocalSessionService  stoped");
    }

    @Override
    public void killOldSessions() throws SystemException {
        for (final String session : sessions.keySet()) {
            final SessionData value = sessions.getQuietly(session);
            if (value != null && (System.currentTimeMillis() - value.getLastDate()) > sessionTimeLive) {
                LOGGER.warn("Local old session killed!:{}", session);
                final SessionData sessionData = sessions.remove(session);
                for (final String sessionId : sessionData.getAllSessions()) {
                    sessions.remove(sessionId);
                }

                rollback0(value,value.getSessionId());
            } else {
                return;
            }
        }
    }

    @Override
    public Map<String, SessionData> getAllSessions() {
        return sessions.getAllData();
    }
}
