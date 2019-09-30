package com.nomad.model;

import java.io.IOException;

import com.nomad.io.MessageInputStream;
import com.nomad.io.MessageOutputStream;
import com.nomad.serializer.Serializer;

public class SaveClientModelSerializer implements Serializer<SaveClientModelImpl> {


    @Override
    public void write(MessageOutputStream out, SaveClientModelImpl data) throws IOException {

        out.writeString(data.getHost());
        out.writeInteger(data.getPort());
        out.writeInteger(data.getThreads());
        out.writeInteger(data.getTimeout());

    }

    @Override
    public SaveClientModelImpl read(MessageInputStream input) throws IOException {

        SaveClientModelImpl result = new SaveClientModelImpl();
        result.setHost(input.readString());
        result.setPort(input.readInteger());
        result.setThreads(input.readInteger());
        result.setTimeout(input.readInteger());

        return result;
    }

}
