package com.nomad.model.management;

import java.io.IOException;

import com.nomad.io.MessageInputStream;
import com.nomad.io.MessageOutputStream;
import com.nomad.serializer.Serializer;

public class ManagementClientModelSerializer implements Serializer<ManagementClientModelImpl> {

    @Override
    public void write(MessageOutputStream out, ManagementClientModelImpl data) throws IOException {

        out.writeString(data.getHost());
        out.writeInteger(data.getPort());
        out.writeInteger(data.getThreads());
        out.writeInteger(data.getTimeout());

    }

    @Override
    public ManagementClientModelImpl read(MessageInputStream input) throws IOException {
        ManagementClientModelImpl result = new ManagementClientModelImpl();
        result.setHost(input.readString());
        result.setPort(input.readInteger());
        result.setThreads(input.readInteger());
        result.setTimeout(input.readInteger());
        return result;
    }

}
