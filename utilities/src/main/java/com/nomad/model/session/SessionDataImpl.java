package com.nomad.model.session;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.nomad.model.SessionData;

public class SessionDataImpl implements SessionData {

    private final String sessionId;
    private long lastDate;
    private final Map<String, SessionData> childSessions = new HashMap<>();
    private String userName;
    private final List<String> roles = new ArrayList<>();

    public SessionDataImpl(final String sessionId) {
        super();
        this.sessionId = sessionId;
        lastDate = System.currentTimeMillis();
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
    public long getLastDate() {
        return lastDate;
    }

    @Override
    public void setLastDate(final long lastDate) {
        this.lastDate = lastDate;
    }

    @Override
    public String getSessionId() {
        return sessionId;
    }

    @Override
    public Map<String, SessionData> getChildSessions() {
        return childSessions;
    }

    @Override
    public String toString() {
        return "SessionData [sessionId=" + sessionId + ", lastDate=" + lastDate + ", childSessions=" + childSessions + "]";
    }

    @Override
    public SessionData searchSession(final String sessionId) {
        if (sessionId.equals(this.sessionId)) {
            return this;
        }
        SessionData result = childSessions.get(sessionId);
        if (result != null) {
            return result;
        }
        for (final SessionData sdata : childSessions.values()) {
            result = sdata.searchSession(sessionId);
            if (result != null) {
                return result;
            }
        }
        return null;
    }

    @Override
    public Set<String> getAllSessions() {
        final Set<String> result = new HashSet<>();
        result.add(sessionId);
        for (final SessionData sdata : childSessions.values()) {
            sdata.getAllSessions(result);
        }
        return result;
    }

    @Override
    public void getAllSessions(final Set<String> result) {
        result.add(sessionId);
        for (final SessionData sdata : childSessions.values()) {
            sdata.getAllSessions(result);
        }
    }

    @Override
    public void remove(final String sessionId) {
        if (childSessions.remove(sessionId) == null) {
            for (final SessionData sdata : childSessions.values()) {
                sdata.remove(sessionId);
            }
        }
    }

    @Override
    public void updateDate() {
        lastDate = System.currentTimeMillis();
    }
}
