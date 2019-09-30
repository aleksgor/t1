package com.nomad.model;

import java.io.IOException;

import com.nomad.exception.SystemException;
import com.nomad.io.MessageInputStream;
import com.nomad.io.MessageOutputStream;
import com.nomad.model.server.ProtocolType;
import com.nomad.serializer.Serializer;

public class CommonServerModelSerializer implements Serializer<CommonServerModelImpl> {

    @Override
    public void write(final MessageOutputStream out, final CommonServerModelImpl data) throws IOException ,SystemException{

        out.writeString( data.getHost() );
        out.writeLong(data.getKeepAliveTime());
        out.writeInteger(data.getMinThreads());
        out.writeInteger(data.getMaxThreads());
        out.writeInteger(data.getPort());
        out.writeString( data.getProtocolType().name() );


    }

    @Override
    public CommonServerModelImpl read(final MessageInputStream input) throws IOException,SystemException {
        final CommonServerModelImpl result = new CommonServerModelImpl();
        result.setHost(input.readString());
        result.setKeepAliveTime(input.readLong());
        result.setMinThreads(input.readInteger());
        result.setMaxThreads(input.readInteger());
        result.setPort(input.readInteger());
        final String string = input.readString();
        if (string != null) {
            result.setProtocolType(ProtocolType.valueOf(string));
        }
        return result;
    }


}
