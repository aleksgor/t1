package com.nomad.model;

import java.io.IOException;
import java.util.Map;

import com.nomad.exception.SystemException;
import com.nomad.io.MessageInputStream;
import com.nomad.io.MessageOutputStream;
import com.nomad.serializer.Serializer;

public class DataSourceModelSerializer implements Serializer<DataSourceModelImpl> {

    @Override
    public void write(MessageOutputStream out, DataSourceModelImpl data) throws IOException,SystemException {


        out.writeString(data.getClazz());
        out.writeString( data.getName());
        out.writeInteger(data.getThreads());
        out.writeInteger(data.getTimeOut());

        out.writeMap(data.getProperties());

    }

    @Override
    @SuppressWarnings("unchecked")
    public DataSourceModelImpl read(MessageInputStream input) throws IOException,SystemException {
        DataSourceModelImpl result = new DataSourceModelImpl();
        result.setClazz(input.readString());
        result.setName(input.readString());
        result.setThreads(input.readInteger());
        result.setTimeOut(input.readInteger());

        Map<String, String> properties = (Map<String, String>) input.readMap();
        result.getProperties().putAll(properties);

        return result;
    }

}
