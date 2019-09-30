package com.nomad.core;

import java.util.Collection;
import java.util.Collections;

import com.nomad.message.MessageHeader;
import com.nomad.model.SessionData;
import com.nomad.model.core.SessionContainer;
import com.nomad.model.session.SessionMessage;

public class SessionContainerImpl implements SessionContainer {
    private String sessionId;
    private String mainSessionId;
    private Collection<String> sessions;

    public SessionContainerImpl(MessageHeader header) {
        this.sessionId = header.getSessionId();
        this.mainSessionId = header.getMainSession();
        if(mainSessionId==null){
            mainSessionId = sessionId;
        }
        this.sessions = header.getSessions();
    }
    public SessionContainerImpl(String sessionId) {
        this.sessionId = sessionId;
        this.mainSessionId = sessionId;
        this.sessions = Collections.singletonList(sessionId);
    }
    public SessionContainerImpl(String sessionId, String mainSessionId, Collection<String>sessions ) {
        this.sessionId = sessionId;
        this.mainSessionId = mainSessionId;
        this.sessions = sessions;
    }

    public SessionContainerImpl(SessionMessage message) {
        sessionId = message.getSessionId();
        mainSessionId = message.getMainSession();
        sessions = message.getSessionIds();
    }
    
    public SessionContainerImpl(SessionData data, String mainSesionId) {
        this.sessionId = data.getSessionId();
        this.mainSessionId = mainSesionId;
        this.sessions = data.getAllSessions();
    }
    @Override
    public String getSessionId() {
        return sessionId;
    }

    @Override
    public String getMainSessionId() {
        return mainSessionId;
    }

    @Override
    public Collection<String> getSessions() {
        return sessions;
    }

    @Override
    public String toString() {
        return "SessionContainerImpl [sessionId=" + sessionId + ", mainSessionId=" + mainSessionId + ", sessions=" + sessions + "]";
    }
    @Override
    public boolean isEmpty() {

        return mainSessionId == null && sessionId == null;
    }

}
