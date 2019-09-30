package com.nomad.model;

import java.io.IOException;
import java.util.Map;

import com.nomad.exception.SystemException;
import com.nomad.io.MessageInputStream;
import com.nomad.io.MessageOutputStream;
import com.nomad.model.server.ProtocolType;
import com.nomad.serializer.Serializer;

public class ListenerModelSerializer implements Serializer<ListenerModelImpl> {

    @Override
    public void write(final MessageOutputStream out, final ListenerModelImpl data) throws IOException,SystemException {
        out.writeString( data.getHost());
        out.writeInteger(data.getPort());
        out.writeString( data.getProtocolVersion());
        out.writeInteger(data.getMinThreads());
        out.writeInteger(data.getMaxThreads());
        out.writeInteger(data.getBacklog());
        out.writeInteger(data.getStatus());
        out.writeString(data.getProtocolType().name());
        final Map<String, String> properties = data.getProperties();
        out.writeMap(properties);

    }

    @Override
    @SuppressWarnings("unchecked")
    public ListenerModelImpl read(final MessageInputStream input) throws IOException,SystemException {
        final ListenerModelImpl result = new ListenerModelImpl();
        result.setHost(input.readString());
        result.setPort(input.readInteger());
        result.setProtocolVersion(input.readString());
        result.setMinThreads(input.readInteger());
        result.setMaxThreads(input.readInteger());
        result.setBacklog(input.readInteger());
        result.setStatus(input.readInteger());
        final String string = input.readString();
        if (string != null) {
            result.setProtocolType(ProtocolType.valueOf(string));
        }

        final Map<? extends String, ? extends String> properties = (Map<String, String>) input.readMap();
        result.getProperties().putAll(properties);


        return result;
    }

}
