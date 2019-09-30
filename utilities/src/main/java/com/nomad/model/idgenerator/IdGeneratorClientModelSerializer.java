package com.nomad.model.idgenerator;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;

import com.nomad.exception.SystemException;
import com.nomad.io.MessageInputStream;
import com.nomad.io.MessageOutputStream;
import com.nomad.model.server.ProtocolType;
import com.nomad.serializer.Serializer;

public class IdGeneratorClientModelSerializer implements Serializer<IdGeneratorClientModelImpl> {

    @Override
    public void write(MessageOutputStream out, IdGeneratorClientModelImpl data) throws IOException, SystemException {
        out.writeInteger(data.getPort());
        out.writeString(data.getHost());
        out.writeInteger(data.getThreads());
        out.writeInteger(data.getTimeout());
        String string = null;
        if (data.getProtocolType() != null) {
            string = data.getProtocolType().name();
        }
        out.writeString(string);
        out.writeMap(data.getProperties());
        out.writeList(data.getModelNames());

    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public IdGeneratorClientModelImpl read(MessageInputStream input) throws IOException, SystemException {
        IdGeneratorClientModelImpl result = new IdGeneratorClientModelImpl();
        result.setPort(input.readInteger());
        result.setHost(input.readString());
        result.setThreads(input.readInteger());
        result.setTimeout(input.readInteger());
        String string = input.readString();
        if (string != null) {
            result.setProtocolType(ProtocolType.valueOf(string));
        }
        result.getProperties().putAll((Map<String, String>) input.readMap());
        input.readList((Collection) result.getModelNames());

        return result;
    }

}
