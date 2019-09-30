package com.nomad.server;

import java.util.Map;

import com.nomad.exception.SystemException;
import com.nomad.model.SessionCallBackServerModel;
import com.nomad.model.SessionData;

public interface SessionService extends ServiceInterface {

    boolean removeSession(String sessionId);

    SessionState startNewSession(String newSession, String userName, String password);

    SessionState getSessionState(String sessionId, String modelName, String operation);

    void killOldSessions() throws SystemException;

    boolean serverRegistering(SessionCallBackServerModel sessionServerModel);

    boolean commit(String sessionId);

    void rollback(String sessionId) throws SystemException;

    SessionState startChildSession(final String parentSessionId, String childSessionId);

    Map<String, SessionData> getAllSessions();
    
    boolean isTrustService();

}
