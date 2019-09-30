package com.nomad.cache.commonclientserver.idgenerator;

import java.io.IOException;
import java.util.Collection;

import com.nomad.exception.SystemException;
import com.nomad.io.MessageInputStream;
import com.nomad.io.MessageOutputStream;
import com.nomad.model.idgenerator.IdGeneratorCommand;
import com.nomad.serializer.Serializer;

public class IdGeneratorMessageSerializer implements Serializer<IdGeneratorMessageImpl> {

    @Override
    public void write(final MessageOutputStream out, final IdGeneratorMessageImpl data) throws IOException, SystemException {

        out.writeString(data.getModelName());
        out.writeList(data.getValue());
        out.writeInteger(data.getResultCode());
        out.writeString(data.getCommand().name());
        out.writeInteger(data.getCount());

    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public IdGeneratorMessageImpl read(final MessageInputStream input) throws IOException, SystemException {

        final IdGeneratorMessageImpl result = new IdGeneratorMessageImpl();

        result.setModelName(input.readString());
        input.readList((Collection) result.getValue());
        result.setResultCode(input.readInteger());
        String string = input.readString();
        if (string != null) {
            result.setCommand(IdGeneratorCommand.valueOf(string));
        }
        result.setCount(input.readInteger());

        return result;
    }

}
