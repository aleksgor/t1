package com.nomad.session;

import com.nomad.server.SessionState;

public class SessionContainer {
    private SessionState session;

    public SessionState getSession() {
        return session;
    }

    public void setSession(SessionState session) {
        this.session = session;
    }

    @Override
    public String toString() {
        return "SessionContainer [session=" + session + "]";
    }

}
