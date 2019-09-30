package com.nomad.message;

import java.util.HashSet;
import java.util.Set;

public class MessageHeader {
    private byte version=0x1;
    private String modelName;
    private String command;
    private String sessionId;
    private final Set<String> sessions=new HashSet<>();
    private  String mainSession;
    private String userName;
    private String password;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Set<String> getSessions() {
        return sessions;
    }

    public String getSessionId() {

        return sessionId;
    }

    public void setSessionId(final String sessionId) {
        this.sessionId = sessionId;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(final String command) {
        this.command = command;
    }

    public byte getVersion() {
        return version;
    }

    public void setVersion(final byte version) {
        this.version = version;
    }


    public String getModelName() {
        return modelName;
    }

    public void setModelName(final String modelName) {
        this.modelName = modelName;
    }


    public void setMainSession(final String mainSession) {
        this.mainSession = mainSession;
    }

    public String getMainSession() {
        return mainSession;
    }

    @Override
    public String toString() {
        return "MessageHeader [version=" + version + ", modelName=" + modelName + ", command=" + command + ", sessionId=" + sessionId + ", sessions=" + sessions + ", mainSession="
                + mainSession + ", userName=" + userName + ", password=" + (password == null ? "null" : "******") + "]";
    }


}
