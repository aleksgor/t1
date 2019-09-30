package com.nomad.session;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import com.nomad.server.SessionResult;
import com.nomad.server.SessionState;

public class SessionStateImpl implements SessionState{
    private String parentSession;
    private String childSession;
    private String user;
    private final List<String> roles = new ArrayList<String>();

    private final Collection<String> childrenSessions= new HashSet<>();
    private SessionResult result;


    public SessionStateImpl(final SessionResult result) {
        super();
        this.result = result;
    }
    @Override
    public String getMainSession() {
        return parentSession;
    }
    @Override
    public void setMainSession(final String parentSession) {
        this.parentSession = parentSession;
    }

    @Override
    public SessionResult getResult() {
        return result;
    }

    @Override
    public void setResult(final SessionResult result) {
        this.result = result;
    }

    @Override
    public String getSessionId() {
        return childSession;
    }

    @Override
    public void setSessionId(final String childSession) {
        this.childSession = childSession;
    }

    @Override
    public Collection<String> getChildrenSessions() {
        return childrenSessions;
    }

    @Override
    public String getUser() {
        return user;
    }

    @Override
    public void setUser(String user) {
        this.user = user;
    }

    @Override
    public List<String> getRoles() {
        return roles;
    }

    @Override
    public String toString() {
        return "SessionStateImpl [parentSession=" + parentSession + ", childSession=" + childSession + ", user=" + user + ", roles=" + roles + ", childrenSessions="
                + childrenSessions + ", result=" + result + "]";
    }

}
