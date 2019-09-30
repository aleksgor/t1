package com.nomad.io.serializer;

import java.io.IOException;

import com.nomad.io.MessageInputStream;
import com.nomad.io.MessageOutputStream;
import com.nomad.io.model.MasterModelId;
import com.nomad.serializer.Serializer;

public class TestIdSerializer implements Serializer<MasterModelId> {

    @Override
    public void write(MessageOutputStream out, MasterModelId data) throws IOException {
        out.writeLong(data.getId());
    }

    @Override
    public MasterModelId read(MessageInputStream input) throws IOException {
        long result = input.readLong();
        return new MasterModelId(result);
    }

}
