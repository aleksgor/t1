package com.nomad.model.session;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.nomad.exception.SystemException;
import com.nomad.io.MessageInputStream;
import com.nomad.io.MessageOutputStream;
import com.nomad.serializer.Serializer;

public class SessionListenerModelSerializer implements Serializer<SessionListenerModel> {

    @Override
    public void write(MessageOutputStream out, SessionListenerModel data) throws IOException, SystemException {
        out.writeString(data.getHost());
        out.writeInteger(data.getPort());
        out.writeInteger(data.getThreads());
        out.writeInteger(data.getBacklog());
        out.writeInteger(data.getStatus());

        Map<Object, Object> properties = new HashMap<>(data.getProperties());
        out.writeMap(properties);

    }

    @Override
    @SuppressWarnings("unchecked")
    public SessionListenerModel read(MessageInputStream input) throws IOException, SystemException {
        SessionListenerModel result = new SessionListenerModel();
        result.setHost(input.readString());
        result.setPort(input.readInteger());
        result.setThreads(input.readInteger());
        result.setBacklog(input.readInteger());
        result.setStatus(input.readInteger());

        Map<Object, Object> properties = (Map<Object, Object>) input.readMap();
        result.getProperties().putAll(properties);

        return result;
    }

}
