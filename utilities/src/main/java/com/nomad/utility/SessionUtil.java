package com.nomad.utility;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import com.nomad.message.MessageHeader;
import com.nomad.model.session.SessionMessage;
import com.nomad.server.SessionResult;
import com.nomad.server.SessionState;

public class SessionUtil {
    public static Collection<String> getAllSessions( final MessageHeader header) {
        final Collection<String> result = new HashSet<>();
        if (header != null && header.getSessionId() != null) {
            result.add(header.getSessionId());
            result.addAll(header.getSessions());
        }

        return result;
    }

    public static Collection<String> getAllSessions(final SessionMessage message) {
        final Collection<String> result = new ArrayList<>();
        if(message==null){
            return result;
        }
        if (message.getSessionId() != null) {
            result.add(message.getSessionId());
        }
        if (message.getSessionIds() != null) {
            result.addAll(message.getSessionIds());
        }
        return result;
    }

    public static SessionResult getSessionResult(final int code){
        //  OK(0),Error(-1),AccessDenied(-2),OperationDenied(-3),TimeOut(-4);
        switch (code) {
        case 0:
            return SessionResult.OK;
        case -1:
            return SessionResult.ERROR;
        case -2:
            return SessionResult.ACCESS_DENIED;
        case -3:
            return SessionResult.OPERATION_DENIED;
        case -4:
            return SessionResult.TIME_OUT;
        }
        return SessionResult.ERROR;
    }

    public static  void fillSessions(final MessageHeader header, final SessionState state){
        header.setMainSession(state.getMainSession());
        header.setSessionId(state.getSessionId());
        header.getSessions().clear();
        header.getSessions().addAll(state.getChildrenSessions());
    }
    public static  void fillSessions(final MessageHeader header, final SessionMessage message){
        header.setMainSession(message.getMainSession());
        header.setSessionId(message.getSessionId());
        header.getSessions().clear();
        header.getSessions().addAll(message.getSessionIds());
    }

    public static  void fillSessions(final MessageHeader source, final MessageHeader target){
        target.setMainSession(source.getMainSession());
        target.setSessionId(source.getSessionId());
        target.getSessions().clear();
        target.getSessions().addAll(source.getSessions());
    }

}
