package com.nomad.session;

import java.io.IOException;
import java.util.List;

import com.nomad.exception.SystemException;
import com.nomad.io.MessageInputStream;
import com.nomad.io.MessageOutputStream;
import com.nomad.serializer.Serializer;
import com.nomad.server.SessionResult;

public class SessionStateImplSerializer implements Serializer<SessionStateImpl>{

    @Override
    public void write(final MessageOutputStream out, final SessionStateImpl data) throws IOException, SystemException {
        out.writeList(data.getChildrenSessions());
        out.writeString(data.getMainSession());

        String name=null;
        if(data.getResult()!=null){
            name=data.getResult().name();
        }
        out.writeString(name);
        out.writeString( data.getSessionId());
    }

    @SuppressWarnings("unchecked")
    @Override
    public SessionStateImpl read(final MessageInputStream input) throws IOException, SystemException {
        final SessionStateImpl result= new SessionStateImpl(null);
        result.getChildrenSessions().addAll((List<String>)input.readList());
        result.setMainSession(input.readString());
        final String string= input.readString();
        if(string!=null){
            result.setResult(SessionResult.valueOf(string));
        }
        result.setSessionId(input.readString());
        return result;
    }

}
