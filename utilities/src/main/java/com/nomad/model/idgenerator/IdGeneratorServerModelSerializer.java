package com.nomad.model.idgenerator;

import java.io.IOException;

import com.nomad.io.MessageInputStream;
import com.nomad.io.MessageOutputStream;
import com.nomad.serializer.Serializer;

public class IdGeneratorServerModelSerializer implements Serializer<IdGeneratorServerModelImpl> {

    @Override
    public void write(MessageOutputStream out, IdGeneratorServerModelImpl data) throws IOException {
        out.writeInteger(data.getPort());
        out.writeString(data.getHost());
        out.writeInteger(data.getMinThreads());
        out.writeInteger(data.getMaxThreads());
        out.writeLong(data.getKeepAliveTime());
        out.writeInteger(data.getIncrement());
        out.writeInteger(data.getTimeOut());

    }

    @Override
    public IdGeneratorServerModelImpl read(MessageInputStream input) throws IOException {
        IdGeneratorServerModelImpl result = new IdGeneratorServerModelImpl();
        result.setPort(input.readInteger());
        result.setHost(input.readString());
        result.setMinThreads(input.readInteger());
        result.setMaxThreads(input.readInteger());
        result.setKeepAliveTime(input.readLong());
        result.setIncrement(input.readInteger());
        result.setTimeOut(input.readInteger());
        return result;
    }

}
