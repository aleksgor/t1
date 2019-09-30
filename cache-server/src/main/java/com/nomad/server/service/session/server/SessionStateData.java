package com.nomad.server.service.session.server;

import com.nomad.model.SessionData;
import com.nomad.server.SessionState;

public class SessionStateData {
    private final SessionState sessionState;
    private final SessionData sessionData;
    public SessionState getSessionState() {
        return sessionState;
    }
    public SessionData getSessionData() {
        return sessionData;
    }
    public SessionStateData(final SessionState sessionState, final SessionData sessionData) {
        super();
        this.sessionState = sessionState;
        this.sessionData = sessionData;
    }


}
