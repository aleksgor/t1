package com.nomad.server.service.saveservice.model;

import java.io.IOException;
import java.util.Collection;

import com.nomad.exception.SystemException;
import com.nomad.io.MessageInputStream;
import com.nomad.io.MessageOutputStream;
import com.nomad.serializer.Serializer;

public class SaveResultSerializer implements Serializer<SaveResultImpl> {


    @Override
    public void write(final MessageOutputStream out, final SaveResultImpl data) throws IOException,SystemException {

        out.writeList(data.getAllowedIds());
        out.writeInteger(data.getResultCode());

    }

    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public SaveResultImpl read(final MessageInputStream input) throws IOException,SystemException {

        final Collection l=input.readList();
        final SaveResultImpl result = new SaveResultImpl(l);
        result.setResultCode(input.readInteger());

        return result;
    }

}
