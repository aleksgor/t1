package com.nomad.model.session;

import java.io.IOException;

import com.nomad.io.MessageInputStream;
import com.nomad.io.MessageOutputStream;
import com.nomad.serializer.Serializer;

public class SessionClientModelSerializer implements Serializer<SessionClientModelImpl> {

    @Override
    public void write(MessageOutputStream out, SessionClientModelImpl data) throws IOException {
        out.writeInteger(data.getPort());
        out.writeString(data.getHost());
        out.writeInteger(data.getThreads());
        out.writeInteger(data.getTimeout());

    }

    @Override
    public SessionClientModelImpl read(MessageInputStream input) throws IOException {
        SessionClientModelImpl result = new SessionClientModelImpl();
        result.setPort(input.readInteger());
        result.setHost(input.readString());
        result.setThreads(input.readInteger());
        result.setTimeout(input.readInteger());
        return result;
    }

}
