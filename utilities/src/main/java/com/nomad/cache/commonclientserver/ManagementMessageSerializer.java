package com.nomad.cache.commonclientserver;

import java.io.IOException;

import com.nomad.exception.SystemException;
import com.nomad.io.MessageInputStream;
import com.nomad.io.MessageOutputStream;
import com.nomad.message.Result;
import com.nomad.serializer.Serializer;

public class ManagementMessageSerializer implements Serializer<ManagementMessageImpl> {

    @Override
    public void write(final MessageOutputStream out, final ManagementMessageImpl data) throws IOException, SystemException {

        out.writeString(data.getCommand());
        out.writeObject(data.getData());
        out.writeString(data.getModelName());
        out.writeObject(data.getResult());
    }

    @Override
    public ManagementMessageImpl read(final MessageInputStream input) throws IOException, SystemException {
        final ManagementMessageImpl result = new ManagementMessageImpl();
        result.setCommand(input.readString());
        result.setData(input.readObject());
        result.setModelName(input.readString());
        result.setResult((Result) input.readObject());
        return result;
    }

}
