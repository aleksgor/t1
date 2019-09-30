package com.nomad.model;

import java.io.IOException;

import com.nomad.exception.SystemException;
import com.nomad.io.MessageInputStream;
import com.nomad.io.MessageOutputStream;
import com.nomad.model.StoreModel.StoreType;
import com.nomad.serializer.Serializer;

public class StoreModelSerializer implements Serializer<StoreModelImpl> {

    @Override
    public void write(MessageOutputStream out, StoreModelImpl data) throws IOException, SystemException {

        out.writeString(data.getDataSource());
        out.writeString(data.getModel());
        out.writeString(data.getClazz());
        out.writeBoolean(data.isReadThrough());
        out.writeBoolean(data.isWriteThrough());

        out.writeString(data.getStoreType() == null ? null : data.getStoreType().name());
        out.writeInteger(data.getCopyCount());
        out.writeObject(data.getCacheMatcherModel());
    }

    @Override
    public StoreModelImpl read(MessageInputStream input) throws IOException, SystemException {
        StoreModelImpl result = new StoreModelImpl();
        result.setDataSource(input.readString());
        result.setModel(input.readString());
        result.setClazz(input.readString());
        result.setReadThrough(input.readBoolean());
        result.setWriteThrough(input.readBoolean());
        String string = input.readString();
        if (string != null) {
            result.setStoreType(StoreType.valueOf(string));
        }
        result.setCopyCount(input.readInteger());
        result.setCacheMatcherModel((CacheMatcherModelImpl) input.readObject());
        return result;
    }

}
