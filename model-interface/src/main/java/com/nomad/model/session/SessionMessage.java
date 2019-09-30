package com.nomad.model.session;

import java.util.List;

import com.nomad.message.CommonMessage;

public interface SessionMessage extends CommonMessage {

    List<String> getSessionIds();

    SessionCommand getSessionCommand();

    void setSessionCommand(final SessionCommand sessionCommand);

    String getOperation();

    void setOperation(final String operation);

    String getModelName();

    void setModelName(final String modelName);

    String getMainSession();

    void setMainSession(final String mainSession);

    String getSessionId();

    void setSessionId(final String sessionId);

    String getUserName();

    void setUserName(String userName);

    List<String> getRoles();

    void setPassword(String password);

    String getPassword();
}
