package com.nomad.model;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.nomad.exception.SystemException;
import com.nomad.io.MessageInputStream;
import com.nomad.io.MessageOutputStream;
import com.nomad.serializer.Serializer;

public class CommandPluginModelSerializer implements Serializer<CommandPluginModelImpl> {

    @Override
    public void write(MessageOutputStream out, CommandPluginModelImpl data) throws IOException ,SystemException{

        out.writeString( data.getClazz());
        out.writeInteger(data.getPoolSize());
        out.writeLong( data.getCheckDelay());
        out.writeInteger(data.getTimeout());
        Map<Object, Object> properties = new HashMap<>(data.getProperties());
        out.writeMap(properties);


    }

    @Override
    @SuppressWarnings("unchecked")
    public CommandPluginModelImpl read(MessageInputStream input) throws IOException,SystemException {
        CommandPluginModelImpl result = new CommandPluginModelImpl();
        result.setClazz(input.readString());
        result.setPoolSize(input.readInteger());
        result.setCheckDelay(input.readLong());
        result.setTimeout(input.readInteger());
        Map<Object, Object> properties = (Map<Object, Object>) input.readMap();
        result.getProperties().putAll(properties);

        return result;
    }

}
