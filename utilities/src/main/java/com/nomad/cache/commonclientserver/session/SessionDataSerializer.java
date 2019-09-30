package com.nomad.cache.commonclientserver.session;

import java.io.IOException;
import java.util.Map;

import com.nomad.exception.SystemException;
import com.nomad.io.MessageInputStream;
import com.nomad.io.MessageOutputStream;
import com.nomad.model.SessionData;
import com.nomad.model.session.SessionDataImpl;
import com.nomad.serializer.Serializer;

public class SessionDataSerializer implements Serializer<SessionDataImpl> {

    @Override
    public void write(final MessageOutputStream out, final SessionDataImpl data) throws IOException, SystemException {

        out.writeString(data.getSessionId());
        out.writeMap(data.getChildSessions());
        out.writeLong(data.getLastDate());
    }

    @SuppressWarnings({ "unchecked" })
    @Override
    public SessionDataImpl read(final MessageInputStream input) throws IOException, SystemException {
        final SessionDataImpl result = new SessionDataImpl(input.readString());
        result.getChildSessions().putAll((Map<String, SessionData>)input.readMap());
        result.setLastDate(input.readLong());
        return result;
    }

}
