package com.nomad.cache.commonclientserver.session;

import java.util.ArrayList;
import java.util.List;

import com.nomad.model.session.SessionCommand;
import com.nomad.model.session.SessionMessage;

public class SessionMessageImpl implements  SessionMessage {

    private final List<String> sessionIds= new ArrayList<>();
    private SessionCommand sessionCommand;
    private String operation;
    private String modelName;
    private String mainSession;
    private String userName;
    private String password;
    private final List<String> roles = new ArrayList<>();

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public void setPassword(String password) {
        this.password = password;
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
    public List<String> getSessionIds() {
        return sessionIds;
    }

    @Override
    public SessionCommand getSessionCommand() {
        return sessionCommand;
    }

    @Override
    public void setSessionCommand(final SessionCommand sessionCommand) {
        this.sessionCommand = sessionCommand;
    }

    @Override
    public String getOperation() {
        return operation;
    }

    @Override
    public void setOperation(final String operation) {
        this.operation = operation;
    }

    @Override
    public String getModelName() {
        return modelName;
    }

    @Override
    public void setModelName(final String modelName) {
        this.modelName = modelName;
    }

    @Override
    public String getMainSession() {
        return mainSession;
    }

    @Override
    public void setMainSession(final String mainSession) {
        this.mainSession = mainSession;
    }



    @Override
    public String toString() {
        return "SessionMessageImpl [sessionIds=" + sessionIds + ", sessionCommand=" + sessionCommand + ", operation=" + operation + ", modelName=" + modelName + ", mainSession="
                + mainSession + ", userName=" + userName + ", roles=" + roles + "]";
    }

    @Override
    public String getSessionId() {
        if(sessionIds.iterator().hasNext()){
            return sessionIds.iterator().next();
        }
        return null;
    }

    @Override
    public void setSessionId(final String sessionId) {
        sessionIds.clear();
        sessionIds.add(sessionId);

    }

}
