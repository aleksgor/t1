package com.nomad.cache.commonclientserver.session;

import java.io.IOException;
import java.util.List;

import com.nomad.exception.SystemException;
import com.nomad.io.MessageInputStream;
import com.nomad.io.MessageOutputStream;
import com.nomad.model.SessionData;
import com.nomad.serializer.Serializer;

public class SessionMessageAnswerSerializer implements Serializer<SessionAnswerImpl> {

    @Override
    public void write(final MessageOutputStream out, final SessionAnswerImpl data) throws IOException, SystemException {

        out.writeInteger(data.getResultCode());
        out.writeString(data.getSessionId());
        out.writeObject(data.getSyncData());
        out.writeString(data.getParentSessionId());
        out.writeList( data.getChildSessions());
        out.writeString(data.getUserName());
        out.writeList(data.getRoles());

    }

    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public SessionAnswerImpl read(final MessageInputStream input) throws IOException, SystemException {

        final SessionAnswerImpl result = new SessionAnswerImpl();

        result.setResultCode(input.readInteger());
        result.setSessionId(input.readString());
        result.setSyncData((SessionData) input.readObject());
        result.setParentSessionId(input.readString());
        result.getChildSessions().addAll((List)input.readList());
        result.setUserName(input.readString());
        result.getRoles().addAll((List) input.readList());
        return result;
    }

}
