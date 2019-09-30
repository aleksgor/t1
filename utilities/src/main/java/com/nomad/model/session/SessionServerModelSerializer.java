package com.nomad.model.session;

import java.io.IOException;

import com.nomad.io.MessageInputStream;
import com.nomad.io.MessageOutputStream;
import com.nomad.serializer.Serializer;

public class SessionServerModelSerializer implements Serializer<SessionServerModelImp> {

    @Override
    public void write(MessageOutputStream out, SessionServerModelImp data) throws IOException {
        out.writeInteger(data.getPort());
        out.writeString(data.getHost());
        out.writeInteger(data.getMinThreads());
        out.writeInteger(data.getMaxThreads());
        out.writeLong(data.getKeepAliveTime());
        out.writeLong(data.getSessionTimeLive());

    }

    @Override
    public SessionServerModelImp read(MessageInputStream input) throws IOException {
        SessionServerModelImp result = new SessionServerModelImp();
        result.setPort(input.readInteger());
        result.setHost(input.readString());
        result.setMinThreads(input.readInteger());
        result.setMaxThreads(input.readInteger());
        result.setKeepAliveTime(input.readLong());
        result.setSessionTimeLive(input.readLong());
        return result;
    }

}
