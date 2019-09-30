package com.nomad.client;



public  class SessionResult   extends AbstractResult {
    private final  String sessionId;

    public SessionResult(final String sessionId) {
        super();
        this.sessionId = sessionId;
    }
    public SessionResult() {
        super();
        sessionId=null;
    }

    public String getSessionId() {
        return sessionId;
    }


}
