package com.nomad.server.service.saveservice.model;

import java.io.IOException;
import java.util.Collection;

import com.nomad.exception.SystemException;
import com.nomad.io.MessageInputStream;
import com.nomad.io.MessageOutputStream;
import com.nomad.message.SaveCommand;
import com.nomad.serializer.Serializer;

public class SaveRequestSerializer implements Serializer<SaveRequestImpl> {


    @Override
    public void write(final MessageOutputStream out, final SaveRequestImpl data) throws IOException,SystemException {

        out.writeString(data.getCommand().name());
        out.writeLong(data.getClientId());
        out.writeList(data.getIdentifiers());
        out.writeList(data.getSessionIds());

    }

    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public SaveRequestImpl read(final MessageInputStream input) throws IOException,SystemException {

        final String command= input.readString();
        final long clientId=input.readLong();
        final SaveRequestImpl result = new SaveRequestImpl(SaveCommand.valueOf(command),clientId);
        result.getIdentifiers().addAll((Collection) input.readList());
        result.getSessionIds().addAll((Collection) input.readList());
        return result;
    }

}
