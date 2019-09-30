package com.nomad.server;

import java.util.Collection;
import java.util.List;

public interface SessionState {

    String getMainSession();

    void setMainSession(String parentSession);

    SessionResult getResult();

    void setResult(SessionResult result);

    String getSessionId() ;

    void setSessionId(final String childSession) ;

    Collection<String> getChildrenSessions();

    String getUser();

    void setUser(String user);

    List<String> getRoles();
}