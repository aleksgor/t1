package com.nomad.server;

import java.io.IOException;

import com.nomad.exception.SystemException;
import com.nomad.io.MessageInputStream;
import com.nomad.io.MessageOutputStream;
import com.nomad.serializer.Serializer;

public class StringMessageSerializer implements Serializer<StringMessage>{

    @Override
    public void write(final MessageOutputStream out, final StringMessage data) throws IOException, SystemException {
        out.writeString(data.getData());

    }

    @Override
    public StringMessage read(final MessageInputStream input) throws IOException, SystemException {

        return new  StringMessage(input.readString());
    }

}
