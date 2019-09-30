package com.nomad.server.service.session.server;

import java.util.Map;
import java.util.UUID;

import com.nomad.exception.SystemException;
import com.nomad.model.SessionCallBackServerModel;
import com.nomad.model.SessionData;
import com.nomad.server.SessionResult;
import com.nomad.server.SessionService;
import com.nomad.server.SessionState;
import com.nomad.session.SessionStateImpl;

public class TrustSessionService implements SessionService {

    @Override
    public void start() throws SystemException {

    }

    @Override
    public boolean removeSession(final String sessionId) {

        return true;
    }

    @Override
    public SessionState startNewSession(String sessionId, String userName, String password) {
        if (sessionId == null) {
            sessionId = UUID.randomUUID().toString();
        }
        final SessionState result = new SessionStateImpl(SessionResult.OK);
        result.setMainSession(sessionId);
        result.setSessionId(sessionId);
        result.getChildrenSessions().add(sessionId);
        return result;
    }

    @Override
    public SessionState getSessionState(final String sessionId, final String modelName, final String operation) {
        SessionState result = new SessionStateImpl(SessionResult.OK);
        result.setMainSession(sessionId);
        return result;
    }

    @Override
    public void killOldSessions() {

    }

    @Override
    public boolean serverRegistering(final SessionCallBackServerModel sessionServerModel) {
        return true;
    }

    @Override
    public void stop() {

    }

    @Override
    public boolean commit(final String sessionId) {
        return true;
    }

    @Override
    public void rollback(final String sessionId) {

    }

    @Override
    public SessionState startChildSession(final String parentSessionId, final String childSessionId) {
        return null;
    }

    @Override
    public Map<String, SessionData> getAllSessions() {
        // TODO Auto-generated method stub
        return null;
    }
    @Override
    public boolean isTrustService() {
        return true;
    }

}
