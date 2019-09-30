package com.nomad.model;

import java.io.IOException;

import com.nomad.exception.SystemException;
import com.nomad.io.MessageInputStream;
import com.nomad.io.MessageOutputStream;
import com.nomad.serializer.Serializer;

public class CommonClientModelSerializer implements Serializer<CommonClientModelImpl> {

    @Override
    public void write(MessageOutputStream out, CommonClientModelImpl data) throws IOException, SystemException {

        out.writeString(data.getHost());
        out.writeInteger(data.getPort());
        out.writeInteger(data.getThreads());
        out.writeInteger(data.getTimeout());

    }

    @Override
    public CommonClientModelImpl read(MessageInputStream input) throws IOException, SystemException {

        CommonClientModelImpl result = new CommonClientModelImpl();
        result.setHost(input.readString());
        result.setPort(input.readInteger());
        result.setThreads(input.readInteger());
        result.setTimeout(input.readInteger());

        return result;
    }

}
