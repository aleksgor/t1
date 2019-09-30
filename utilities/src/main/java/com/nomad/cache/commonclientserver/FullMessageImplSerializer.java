package com.nomad.cache.commonclientserver;

import java.io.IOException;

import com.nomad.exception.SystemException;
import com.nomad.io.MessageInputStream;
import com.nomad.io.MessageOutputStream;
import com.nomad.message.Body;
import com.nomad.message.MessageHeader;
import com.nomad.message.Result;
import com.nomad.serializer.Serializer;

public class FullMessageImplSerializer implements Serializer<FullMessageImpl> {

    @Override
    public void write(MessageOutputStream out, FullMessageImpl data) throws IOException,SystemException {

        out.writeObject( data.getHeader());
        out.writeObject(data.getResult());
        out.writeObject(data.getBody());

    }

    @Override
    public FullMessageImpl read(MessageInputStream input) throws IOException,SystemException {
        FullMessageImpl result = new FullMessageImpl();
        result.setHeader((MessageHeader)input.readObject());
        result.setResult((Result)input.readObject());
        result.setMessage((Body)input.readObject());

        return result;
    }

}
