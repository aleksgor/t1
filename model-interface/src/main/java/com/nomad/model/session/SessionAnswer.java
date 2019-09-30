package com.nomad.model.session;

import java.util.List;
import java.util.Set;

import com.nomad.message.CommonAnswer;
import com.nomad.model.SessionData;

public interface SessionAnswer extends CommonAnswer {

    SessionData getSyncData() ;

    void setSyncData(final SessionData syncData) ;

    String getSessionId() ;

    void setSessionId(final String sessionId) ;

    void setResultCode(final int resultCode) ;

    String getParentSessionId() ;

    void setParentSessionId(final String parentSessionId) ;

    Set<String> getChildSessions();

    List<String> getRoles();

    void setUserName(String userName);

    String getUserName();

}
