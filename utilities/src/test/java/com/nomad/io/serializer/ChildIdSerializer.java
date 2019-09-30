package com.nomad.io.serializer;

import java.io.IOException;

import com.nomad.io.MessageInputStream;
import com.nomad.io.MessageOutputStream;
import com.nomad.io.model.ChildModelId;
import com.nomad.serializer.Serializer;

public class ChildIdSerializer implements Serializer<ChildModelId> {

    @Override
    public void write(MessageOutputStream out, ChildModelId data) throws IOException {
        out.writeLong(data.getId());
    }

    @Override
    public ChildModelId read(MessageInputStream input) throws IOException {
        long result = input.readLong();
        return new ChildModelId(result);
    }

}
