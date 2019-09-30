package com.nomad.model;

import java.io.IOException;
import java.util.Map;

import com.nomad.exception.SystemException;
import com.nomad.io.MessageInputStream;
import com.nomad.io.MessageOutputStream;
import com.nomad.serializer.Serializer;

public class CacheMatcherSerializer implements Serializer<CacheMatcherModelImpl> {

    @Override
    public void write(MessageOutputStream out, CacheMatcherModelImpl data) throws IOException, SystemException {

        out.writeString(data.getClazz());
        out.writeMap(data.getProperties());

    }

    @SuppressWarnings("unchecked")
    @Override
    public CacheMatcherModelImpl read(MessageInputStream input) throws IOException, SystemException {

        CacheMatcherModelImpl result = new CacheMatcherModelImpl();
        result.setClazz(input.readString());
        result.getProperties().putAll((Map<? extends String, ? extends String>) input.readMap());

        return result;
    }

}
