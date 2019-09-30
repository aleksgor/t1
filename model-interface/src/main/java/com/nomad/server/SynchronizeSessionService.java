package com.nomad.server;

import java.util.Map;

import com.nomad.model.SessionData;
import com.nomad.model.session.SessionAnswer;



public interface SynchronizeSessionService extends ServiceInterface {

    void removeSession(String sessionId) ;

    void startNewSession(String newSession);

    SessionAnswer getSessionState(String sessionId, String modelName, String operation) ;

    void startChildSession(final String parentSessionId, String childSessionId);

    Map<String, SessionData> getAllSessionData();

}
