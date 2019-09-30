package com.nomad.io.serializer;

import java.io.IOException;

import com.nomad.exception.SystemException;
import com.nomad.io.MessageInputStream;
import com.nomad.io.MessageOutputStream;
import com.nomad.io.model.ChildModel;
import com.nomad.io.model.ChildModelId;
import com.nomad.serializer.Serializer;

public class ChildSerializer implements Serializer<ChildModel> {
    @Override
    public void write(MessageOutputStream out, ChildModel data) throws IOException, SystemException {
        out.writeObject(data.getIdentifier());
        out.writeString(data.getName());
        out.writeLong(data.getId());

    }

    @Override
    public ChildModel read(MessageInputStream input) throws IOException, SystemException {
        ChildModel result = new ChildModel();
        result.setIdentifier((ChildModelId) input.readObject());
        result.setName(input.readString());
        result.setId(input.readLong());
        return result;
    }

}
