package com.nomad.model;

import java.io.IOException;

import com.nomad.io.MessageInputStream;
import com.nomad.io.MessageOutputStream;
import com.nomad.serializer.Serializer;

public class SaveServerModelSerializer implements Serializer<SaveServerModelImpl> {


    @Override
    public void write(MessageOutputStream out, SaveServerModelImpl data) throws IOException {

        out.writeString(data.getHost());
        out.writeInteger(data.getPort());
        out.writeInteger(data.getMinThreads());
        out.writeInteger(data.getMaxThreads());
        out.writeLong(data.getSessionTimeout());


    }

    @Override
    public SaveServerModelImpl read(MessageInputStream input) throws IOException {

        SaveServerModelImpl result = new SaveServerModelImpl();
        result.setHost(input.readString());
        result.setPort(input.readInteger());
        result.setMinThreads(input.readInteger());
        result.setMaxThreads(input.readInteger());
        result.setSessionTimeout(input.readLong());

        return result;
    }

}
