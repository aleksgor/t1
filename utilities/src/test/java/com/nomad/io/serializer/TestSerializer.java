package com.nomad.io.serializer;

import java.io.IOException;
import java.util.Date;

import com.nomad.exception.SystemException;
import com.nomad.io.MessageInputStream;
import com.nomad.io.MessageOutputStream;
import com.nomad.io.model.ChildModel;
import com.nomad.io.model.MasterModel;
import com.nomad.io.model.MasterModelId;
import com.nomad.serializer.Serializer;

public class TestSerializer implements Serializer<MasterModel> {

    @Override
    public void write(MessageOutputStream out, MasterModel data) throws IOException, SystemException {
        out.writeObject(data.getChild());
        out.writeLong(data.getChildId());
        out.writeObject(data.getDate());
        out.writeObject(data.getIdentifier());
        out.writeString(data.getName());
        out.writeLong(data.getId());
    }

    @Override
    public MasterModel read(MessageInputStream input) throws IOException, SystemException {
        MasterModel result = new MasterModel();
        result.setChild((ChildModel) input.readObject());
        result.setChildId(input.readLong());
        result.setDate((Date) input.readObject());
        result.setIdentifier((MasterModelId) input.readObject());
        result.setName(input.readString());
        result.setId(input.readLong());
        return result;
    }

}
