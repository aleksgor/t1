package com.nomad.model;

import java.io.IOException;

import com.nomad.io.MessageInputStream;
import com.nomad.io.MessageOutputStream;
import com.nomad.serializer.Serializer;

public class SessionCallBackServerModelSerializer implements Serializer<SessionCallBackServerModelImp> {

    @Override
    public void write(MessageOutputStream out, SessionCallBackServerModelImp data) throws IOException {
        out.writeString(data.getHost());
        out.writeLong(data.getKeepAliveTime());
        out.writeInteger(data.getMinThreads());
        out.writeInteger(data.getMaxThreads());
        out.writeInteger(data.getPort());

    }

    @Override
    public SessionCallBackServerModelImp read(MessageInputStream input) throws IOException {
        SessionCallBackServerModelImp result = new SessionCallBackServerModelImp();
        result.setHost(input.readString());
        result.setKeepAliveTime(input.readLong());
        result.setMinThreads(input.readInteger());
        result.setMaxThreads(input.readInteger());
        result.setPort(input.readInteger());
        return result;
    }

}
