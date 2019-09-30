package com.nomad.cache.commonclientserver.session;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.nomad.model.SessionData;
import com.nomad.model.session.SessionAnswer;

public class SessionAnswerImpl implements SessionAnswer {
    // SessionResult  OK(0),Error(-1),AccessDenied(-2),OperationDenied(-3),TimeOut(-4);
    private int resultCode = -1;
    private SessionData syncData;
    private String sessionId;
    private String parentSessionId;
    private final Set<String> childSessions = new HashSet<>();
    private String userName;
    private final List<String> roles = new ArrayList<>();

    public SessionAnswerImpl() {
    }

    public SessionAnswerImpl(final int resultCode, final String sessionId) {
        super();
        this.resultCode = resultCode;
        this.sessionId = sessionId;
    }

    @Override
    public SessionData getSyncData() {
        return syncData;
    }

    @Override
    public void setSyncData(final SessionData syncData) {
        this.syncData = syncData;
    }

    @Override
    public String getSessionId() {
        return sessionId;
    }

    @Override
    public void setSessionId(final String sessionId) {
        this.sessionId = sessionId;
    }

    @Override
    public int getResultCode() {
        return resultCode;
    }

    @Override
    public void setResultCode(final int resultCode) {
        this.resultCode = resultCode;
    }

    @Override
    public String getParentSessionId() {
        return parentSessionId;
    }

    @Override
    public void setParentSessionId(final String parentSessionId) {
        this.parentSessionId = parentSessionId;
    }


    @Override
    public Set<String> getChildSessions() {
        return childSessions;
    }


    @Override
    public String getUserName() {
        return userName;
    }

    @Override
    public void setUserName(String userName) {
        this.userName = userName;
    }

    @Override
    public List<String> getRoles() {
        return roles;
    }

    @Override
    public String toString() {
        return "SessionAnswerImpl [resultCode=" + resultCode + ", syncData=" + syncData + ", sessionId=" + sessionId + ", parentSessionId=" + parentSessionId + ", childsessions="
                + childSessions + "]";
    }

}
