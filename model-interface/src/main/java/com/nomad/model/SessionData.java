package com.nomad.model;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface SessionData {

    String getUserName();

    void setUserName(String userName);

    List<String> getRoles();

    long getLastDate();

    void setLastDate(final long lastDate);

    String getSessionId();

    Map<String, SessionData> getChildSessions();

    SessionData searchSession(final String sessionId);

    void getAllSessions(final Set<String> result);

    void remove(final String sessionId);

    void updateDate();

    Set<String> getAllSessions();
}
